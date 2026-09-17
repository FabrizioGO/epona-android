-- Storage buckets and object policies for pet and sighting photos.
--
-- Nothing in this directory has ever created these. That is precisely why
-- StorageService, PetRepository.uploadPetPhoto and SightingRepository.uploadSightingPhoto
-- shipped as dead code: there was nowhere for the bytes to go. This has to land before
-- a client that uploads reaches users, or every save carrying a photo fails with
-- {"error":"Bucket not found"}.
--
-- Two things the Kotlin client constrains, both easy to get wrong:
--
--   1. The buckets must be public. StorageService.upload returns storage.publicUrl(path)
--      and that string is written straight into pets.photo_urls / sightings.photo_urls,
--      where Coil later GETs it with no Authorization header -- see PetCard and
--      DetailHero. A private bucket answers 400 to those GETs, so every thumbnail in
--      the app renders broken long after the upload appeared to succeed.
--
--   2. The first path segment is the uploader's user id. PhotoUploader writes
--      "{auth.uid()}/{uuid}.jpg" and never names the pet or the sighting, which is what
--      lets a photo be uploaded before its row exists -- a new pet has no id until its
--      insert returns. The insert policy below turns that convention into a rule.

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
    ('pets',      'pets',      true, 5242880, array['image/jpeg', 'image/png', 'image/webp']),
    ('sightings', 'sightings', true, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do nothing;


-- ---------------------------------------------------------------------------
-- Object policies
--
-- storage.objects already has RLS enabled by Supabase; only the policies are ours.
-- Dropped by name first so re-running this file is not an error -- there is no
-- "create or replace policy".
-- ---------------------------------------------------------------------------
drop policy if exists "epona_media_public_read"  on storage.objects;
drop policy if exists "epona_media_owner_insert" on storage.objects;
drop policy if exists "epona_media_owner_update" on storage.objects;
drop policy if exists "epona_media_owner_delete" on storage.objects;

-- anon is included deliberately: a shared alert link is opened by people with no
-- session, and a public bucket only authenticated users may read from is a private
-- bucket with extra steps.
create policy "epona_media_public_read"
on storage.objects for select
to anon, authenticated
using (bucket_id in ('pets', 'sightings'));

-- storage.foldername(name) splits the object path into a text[]; [1] is the first
-- segment, which PhotoUploader sets to the signed-in user id.
create policy "epona_media_owner_insert"
on storage.objects for insert
to authenticated
with check (
    bucket_id in ('pets', 'sightings')
    and (storage.foldername(name))[1] = auth.uid()::text
);

-- StorageService.upload defaults upsert = true, which turns a repeated path into an
-- update rather than an insert. PhotoUploader uses random names so this should never
-- fire, but the policy has to exist for the day something reuses a path.
create policy "epona_media_owner_update"
on storage.objects for update
to authenticated
using (
    bucket_id in ('pets', 'sightings')
    and (storage.foldername(name))[1] = auth.uid()::text
)
with check (
    bucket_id in ('pets', 'sightings')
    and (storage.foldername(name))[1] = auth.uid()::text
);

-- Load-bearing rather than housekeeping: this is what lets PhotoUploader unwind the
-- objects it already sent when a later one in the same batch fails. Without it those
-- bytes are unreachable litter.
create policy "epona_media_owner_delete"
on storage.objects for delete
to authenticated
using (
    bucket_id in ('pets', 'sightings')
    and (storage.foldername(name))[1] = auth.uid()::text
);

-- No `notify pgrst` here, unlike the other files in this directory: storage.buckets
-- and storage.objects are not in the exposed schema, so the PostgREST schema cache has
-- nothing to reload.
