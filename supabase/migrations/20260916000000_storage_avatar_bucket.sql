-- Storage bucket and object policies for user avatars.
--
-- The pets/sightings migration (20260830000000) predates profile photos, so an
-- upload to the avatars bucket fails with {"error":"Bucket not found"} and the
-- RLS policies below would refuse it even if the bucket existed.
--
-- Same constraints as the media buckets, both dictated by the Kotlin client:
--
--   1. The bucket must be public. PhotoUploader hands back storage.publicUrl(path)
--      and that string is written straight into users.avatar_url, where Coil later
--      GETs it with no Authorization header. A private bucket answers 400 to those
--      GETs, so the avatar renders broken long after the upload appeared to succeed.
--
--   2. The first path segment is the uploader's user id. PhotoUploader writes
--      "{auth.uid()}/{uuid}.jpg", so the insert policy below turns that convention
--      into a rule, exactly like the pets/sightings one.
--
-- The four epona_media_* policies are dropped and recreated to cover all three
-- buckets. There is no "create or replace policy", so drop-first keeps this file
-- re-runnable, and the bucket insert is on-conflict-do-nothing for the same reason.

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
    ('avatars', 'avatars', true, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do nothing;


-- ---------------------------------------------------------------------------
-- Object policies (pets, sightings, avatars)
-- ---------------------------------------------------------------------------
drop policy if exists "epona_media_public_read"  on storage.objects;
drop policy if exists "epona_media_owner_insert" on storage.objects;
drop policy if exists "epona_media_owner_update" on storage.objects;
drop policy if exists "epona_media_owner_delete" on storage.objects;

-- anon is included deliberately, matching the media buckets: a shared alert link
-- is opened by people with no session, and the reporter/owner avatar renders there.
create policy "epona_media_public_read"
on storage.objects for select
to anon, authenticated
using (bucket_id in ('pets', 'sightings', 'avatars'));

-- storage.foldername(name) splits the object path into a text[]; [1] is the first
-- segment, which PhotoUploader sets to the signed-in user id.
create policy "epona_media_owner_insert"
on storage.objects for insert
to authenticated
with check (
    bucket_id in ('pets', 'sightings', 'avatars')
    and (storage.foldername(name))[1] = auth.uid()::text
);

-- StorageService.upload defaults upsert = true, which turns a repeated path into an
-- update rather than an insert. PhotoUploader uses random names so this should never
-- fire, but the policy has to exist for the day something reuses a path.
create policy "epona_media_owner_update"
on storage.objects for update
to authenticated
using (
    bucket_id in ('pets', 'sightings', 'avatars')
    and (storage.foldername(name))[1] = auth.uid()::text
)
with check (
    bucket_id in ('pets', 'sightings', 'avatars')
    and (storage.foldername(name))[1] = auth.uid()::text
);

-- Load-bearing rather than housekeeping: this is what lets PhotoUploader unwind the
-- objects it already sent when a later one in the same batch fails. Without it those
-- bytes are unreachable litter.
create policy "epona_media_owner_delete"
on storage.objects for delete
to authenticated
using (
    bucket_id in ('pets', 'sightings', 'avatars')
    and (storage.foldername(name))[1] = auth.uid()::text
);

-- No `notify pgrst`: storage.buckets and storage.objects are not in the exposed
-- schema, so the PostgREST schema cache has nothing to reload.
