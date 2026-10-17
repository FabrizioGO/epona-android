-- Adoption posts: people giving pets up for adoption.
--
-- Deliberately NOT part of `alerts`: an adoption post has no location, no pet row,
-- and a title that can describe several animals at once ("Golden Retriever puppies
-- in adoption"). Reusing alerts would mean making last_seen_location and pet_id
-- nullable and teaching get_nearby_alerts, the map and the notification triggers
-- to skip these rows. A separate table keeps all of that untouched.
--
-- Species/size/gender are plain text with CHECKs rather than enum types, because no
-- CREATE TABLE for pets exists in the repo to copy the enum names from.

create table if not exists public.adoption_posts (
    id            uuid primary key default gen_random_uuid(),
    user_id       uuid not null references public.users(id) on delete cascade,
    title         text not null check (char_length(btrim(title)) between 3 and 120),
    species       text not null check (species in ('dog', 'cat', 'other')),
    breed         text,
    pet_name      text,
    age_stage     text check (age_stage in ('baby', 'young', 'adult', 'senior')),
    size          text check (size in ('small', 'medium', 'large')),
    gender        text check (gender in ('male', 'female', 'unknown')),
    description   text,
    -- null means free
    price         numeric(10, 2) check (price is null or price >= 0),
    photo_urls    text[] not null default '{}',
    contact_phone text not null check (char_length(btrim(contact_phone)) > 0),
    status        text not null default 'active' check (status in ('active', 'adopted')),
    adopted_at    timestamptz,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    check (cardinality(photo_urls) <= 3)
);

create index if not exists adoption_posts_status_created_idx
    on public.adoption_posts (status, created_at desc);
create index if not exists adoption_posts_user_idx
    on public.adoption_posts (user_id);


-- ---------------------------------------------------------------------------
-- updated_at / adopted_at
-- ---------------------------------------------------------------------------
drop trigger if exists trg_adoption_posts_stamp on public.adoption_posts;
drop function if exists public.tg_adoption_posts_stamp();

create or replace function public.tg_adoption_posts_stamp()
returns trigger
language plpgsql
security invoker
set search_path = public
as $$
begin
    new.updated_at := now();
    if new.status = 'adopted' and old.status is distinct from 'adopted' then
        new.adopted_at := coalesce(new.adopted_at, now());
    end if;
    return new;
end;
$$;

create trigger trg_adoption_posts_stamp
before update on public.adoption_posts
for each row
execute function public.tg_adoption_posts_stamp();


-- ---------------------------------------------------------------------------
-- RLS. Reads go through the view below; direct table access is owner-only except
-- for active rows, so a stray client query cannot see someone's adopted history.
-- ---------------------------------------------------------------------------
alter table public.adoption_posts enable row level security;

drop policy if exists "adoption_posts_select" on public.adoption_posts;
drop policy if exists "adoption_posts_insert_own" on public.adoption_posts;
drop policy if exists "adoption_posts_update_own" on public.adoption_posts;
drop policy if exists "adoption_posts_delete_own" on public.adoption_posts;

create policy "adoption_posts_select"
on public.adoption_posts for select
to authenticated
using (status = 'active' or user_id = auth.uid());

create policy "adoption_posts_insert_own"
on public.adoption_posts for insert
to authenticated
with check (user_id = auth.uid());

create policy "adoption_posts_update_own"
on public.adoption_posts for update
to authenticated
using (user_id = auth.uid())
with check (user_id = auth.uid());

create policy "adoption_posts_delete_own"
on public.adoption_posts for delete
to authenticated
using (user_id = auth.uid());

revoke all on public.adoption_posts from anon, authenticated;
grant select, insert, delete on public.adoption_posts to authenticated;
-- Owners may only change the status; everything else is immutable once posted.
grant update (status) on public.adoption_posts to authenticated;


-- ---------------------------------------------------------------------------
-- Read model with the owner's name/avatar.
--
-- A plain view runs with its owner's privileges, so it can read users.display_name
-- without opening users to every client. The visibility rule is repeated in the
-- WHERE clause for exactly that reason: RLS on adoption_posts is bypassed here.
-- ---------------------------------------------------------------------------
drop view if exists public.adoption_posts_with_owner;

create view public.adoption_posts_with_owner as
select
    p.id,
    p.user_id,
    p.title,
    p.species,
    p.breed,
    p.pet_name,
    p.age_stage,
    p.size,
    p.gender,
    p.description,
    p.price,
    p.photo_urls,
    p.contact_phone,
    p.status,
    p.adopted_at,
    p.created_at,
    p.updated_at,
    u.display_name as owner_name,
    u.avatar_url   as owner_avatar
from public.adoption_posts p
    join public.users u on u.id = p.user_id
where p.status = 'active'
   or p.user_id = auth.uid();

revoke all on public.adoption_posts_with_owner from anon, authenticated;
grant select on public.adoption_posts_with_owner to authenticated;


-- ---------------------------------------------------------------------------
-- Account deletion: also return this user's adoption photo URLs so the Edge
-- Function removes the Storage objects. The rows themselves go via ON DELETE
-- CASCADE on user_id, but delete_user_data deletes public.users explicitly, so the
-- rows are removed there too. Body is 20260926000100 plus the adoption photo
-- collection and delete.
-- ---------------------------------------------------------------------------
create or replace function public.delete_user_data(p_user_id uuid)
returns text[]
language plpgsql
volatile
security definer
set search_path = public
as $$
declare
    v_photo_urls    text[] := '{}';
    v_found_pet_ids uuid[];
begin
    -- ------------------------------------------------------------------
    -- 1. Collect every photo URL this account owns, before any row that
    --    carries one is deleted. The caller uses this to remove the matching
    --    Storage objects in the avatars/pets/sightings buckets -- rows and
    --    files are deleted independently, so nothing here touches storage.*.
    -- ------------------------------------------------------------------
    select coalesce(array_agg(u.avatar_url) filter (where u.avatar_url is not null), '{}')
    into v_photo_urls
    from public.users u
    where u.id = p_user_id;

    -- pets this user owns outright
    select v_photo_urls || coalesce(array_agg(photo) filter (where photo is not null), '{}')
    into v_photo_urls
    from public.pets p, unnest(p.photo_urls) as photo
    where p.owner_id = p_user_id;

    -- ownerless "found" pets created by this user's own found-pet alerts --
    -- see 20260920000000_found_pets.sql. One pet per alert (create_found_alert
    -- always inserts a fresh row), so capturing the ids here is enough to
    -- delete exactly these pets later without touching anyone else's.
    select coalesce(array_agg(a.pet_id), '{}')
    into v_found_pet_ids
    from public.alerts a
        join public.pets p on p.id = a.pet_id
    where a.user_id = p_user_id
      and p.owner_id is null;

    select v_photo_urls || coalesce(array_agg(photo) filter (where photo is not null), '{}')
    into v_photo_urls
    from public.pets p, unnest(p.photo_urls) as photo
    where p.id = any(v_found_pet_ids);

    -- adoption posts this user created
    select v_photo_urls || coalesce(array_agg(photo) filter (where photo is not null), '{}')
    into v_photo_urls
    from public.adoption_posts ap, unnest(ap.photo_urls) as photo
    where ap.user_id = p_user_id;

    -- sightings this user filed, or that were filed on this user's alerts
    select v_photo_urls || coalesce(array_agg(photo) filter (where photo is not null), '{}')
    into v_photo_urls
    from public.sightings s, unnest(s.photo_urls) as photo
    where s.reporter_id = p_user_id
       or s.alert_id in (select id from public.alerts where user_id = p_user_id);


    -- ------------------------------------------------------------------
    -- 2. Anonymise rather than delete: a report this user filed on someone
    --    else stays for the moderation history it belongs to, but no longer
    --    names this user as the one who filed it. See
    --    20260926000000_content_reports.sql -- the FK's own ON DELETE SET
    --    NULL would do this too once auth.users is deleted in step 4 of the
    --    caller, but this makes it happen inside this transaction rather
    --    than depend on that later, separate statement.
    -- ------------------------------------------------------------------
    update public.content_reports
    set reporter_id = null
    where reporter_id = p_user_id;


    -- ------------------------------------------------------------------
    -- 3. Delete children before parents.
    -- ------------------------------------------------------------------
    delete from public.notifications
    where user_id = p_user_id
       or alert_id in (select id from public.alerts where user_id = p_user_id);

    delete from public.sightings
    where reporter_id = p_user_id
       or alert_id in (select id from public.alerts where user_id = p_user_id);

    delete from public.alerts
    where user_id = p_user_id;

    delete from public.adoption_posts
    where user_id = p_user_id;

    delete from public.pets
    where owner_id = p_user_id
       or id = any(v_found_pet_ids);

    delete from public.users
    where id = p_user_id;

    return v_photo_urls;
end;
$$;

comment on function public.delete_user_data(uuid) is
    'Deletes every row a user''s account owns, in dependency order, and returns '
    'the photo URLs those rows carried so the caller can remove the matching '
    'Storage objects. service_role only -- see the Edge Function in '
    'supabase/functions/delete-account.';

revoke all on function public.delete_user_data(uuid) from public, anon, authenticated;
grant execute on function public.delete_user_data(uuid) to service_role;

notify pgrst, 'reload schema';
