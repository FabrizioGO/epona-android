-- Found-pet posting without forcing finders to adopt the stray.
--
-- Today the only way to post a FOUND alert is to register the stray as your own
-- pet through AddPet, because PetSelectionStep feeds on
-- `pets WHERE owner_id = auth.uid()` and CreateAlertUseCase hard-requires a pet
-- id. That demands a name (the sole validation rule) plus gender/age/microchip
-- the finder cannot know, and the animal then lives forever in "My Pets" and in
-- get_user_stats.total_pets.
--
-- This migration splits the intents:
--   * FOUND alerts are backed by a pet row with NO owner and NO name. Everything
--     the finder knows (species/breed/color/size/description/photos) lives on
--     that row; the alert row points at it with type = 'found'.
--   * LOST alerts keep using the owned-pet path (create_alert) unchanged.
--
-- Conventions (from 20260823000000_create_alert_function.sql and
-- 20260823000100_rpc_functions.sql):
--   * Every optional argument carries a DEFAULT (Postgrest resolves RPCs by the
--     set of argument names that actually arrive).
--   * Returns setof rather than a bare composite (decodeSingle() is
--     decodeList().first()).
--   * Timestamps are timestamptz.
--   * ::text casts on type/status/species/size/gender so reads work whether those
--     columns are text or Postgres enums. On the write side, text parameters that
--     map to enum columns require explicit casts (e.g. p_species::pet_species).
--     alerts.type IS the enum alert_type and keeps the explicit cast pattern
--     create_alert documents (here as a 'found' literal, which coerces on its own).
--   * Ends with `notify pgrst, 'reload schema'`.

-- ---------------------------------------------------------------------------
-- a. Relax the pets table.
--
-- Base CREATE TABLE / RLS DDL is not in the repo, so nullability was confirmed
-- against the live project before writing this: pets.owner_id and pets.name are
-- NOT NULL there. DROP NOT NULL is a no-op where they are already nullable, so
-- this is safe to run in either case.
-- ---------------------------------------------------------------------------
alter table public.pets alter column owner_id drop not null;
alter table public.pets alter column name drop not null;

-- ---------------------------------------------------------------------------
-- b. create_found_alert — one transaction inserting the ownerless pet and the
-- FOUND alert.
--
-- One RPC rather than a client-side pet insert + create_alert avoids an orphan
-- pet row when the second call fails, and sidesteps needing a new RLS insert
-- policy for `owner_id IS NULL`.
--
-- Mirrors create_sighting, which is security definer for the same reason (the
-- writer is not the row's owner) and re-imposes authorisation by hand on its
-- first line.
-- ---------------------------------------------------------------------------
create or replace function public.create_found_alert(
    p_user_id          uuid,
    p_species          text,
    p_lat              double precision,
    p_lng              double precision,
    p_breed            text        default null,
    p_color            text        default null,
    p_size             text        default 'medium',
    p_pet_description  text        default null,
    p_photo_urls       text[]      default '{}',
    p_address          text        default null,
    p_last_seen_at     timestamptz default null,
    p_alert_description text       default null,
    p_contact_phone    text        default null
)
returns setof public.alerts
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    v_pet_id uuid;
begin
    if p_user_id is distinct from auth.uid() then
        raise exception 'A found alert can only be posted as the signed-in user'
            using errcode = '42501';
    end if;

    insert into public.pets (
        owner_id,
        name,
        species,
        breed,
        color,
        size,
        description,
        photo_urls
    )
    values (
        null,
        null,
        p_species::pet_species,
        nullif(p_breed, ''),
        nullif(p_color, ''),
        p_size::pet_size,
        p_pet_description,
        coalesce(p_photo_urls, '{}')
    )
    returning id into v_pet_id;

    return query
    insert into public.alerts (
        pet_id,
        user_id,
        type,
        status,
        last_seen_location,
        last_seen_address,
        last_seen_at,
        description,
        contact_phone
    )
    values (
        v_pet_id,
        p_user_id,
        -- alerts.type is the enum alert_type; the literal coerces on its own
        -- (same as the 'active' literal on the next line).
        'found',
        'active',
        -- ST_MakePoint is (x, y) — longitude first, then latitude.
        st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography,
        p_address,
        coalesce(p_last_seen_at, now()),
        p_alert_description,
        p_contact_phone
    )
    returning *;
end;
$$;

grant execute on function public.create_found_alert(
    uuid, text, double precision, double precision,
    text, text, text, text, text[],
    text, timestamptz, text, text
) to authenticated;

-- ---------------------------------------------------------------------------
-- c. get_alert_detail gains pet_owner_id.
--
-- The RPC currently returns only u.id as owner_id (the alert poster), and
-- AlertMapper assigns it to BOTH Alert.userId and Pet.ownerId. Left as is,
-- opening a found alert would cache the stray as a pet owned by whoever is
-- viewing it, and PetDao.observeMyPets(ownerId) would surface it in My Pets.
-- p.owner_id is now a distinct pet_owner_id uuid output column.
--
-- Changing a RETURNS TABLE shape cannot use CREATE OR REPLACE ("cannot change
-- return type of existing function"), so drop first. No CASCADE deliberately:
-- if something depends on it, the drop fails loudly instead of cascading.
-- ---------------------------------------------------------------------------
drop function if exists public.get_alert_detail(uuid);

create function public.get_alert_detail(p_alert_id uuid)
returns table (
    alert_id          uuid,
    type              text,
    status            text,
    last_seen_lat     double precision,
    last_seen_lng     double precision,
    last_seen_address text,
    last_seen_at      timestamptz,
    alert_description text,
    reward            numeric,
    contact_phone     text,
    sighting_count    integer,
    created_at        timestamptz,
    resolved_at       timestamptz,
    pet_id            uuid,
    pet_name          text,
    species           text,
    breed             text,
    color             text,
    size              text,
    gender            text,
    microchip_id      text,
    pet_description   text,
    pet_photos        text[],
    pet_owner_id      uuid,
    owner_id          uuid,
    owner_name        text,
    owner_avatar      text
)
language sql
stable
security invoker
set search_path = public, extensions
as $$
    select
        a.id,
        a.type::text,
        a.status::text,
        st_y(a.last_seen_location::geometry),
        st_x(a.last_seen_location::geometry),
        a.last_seen_address,
        a.last_seen_at,
        a.description,
        a.reward,
        a.contact_phone,
        a.sighting_count,
        a.created_at,
        a.resolved_at,
        p.id,
        p.name,
        p.species::text,
        p.breed,
        p.color,
        p.size::text,
        p.gender::text,
        p.microchip_id,
        p.description,
        p.photo_urls,
        p.owner_id,
        u.id,
        u.display_name,
        u.avatar_url
    from public.alerts a
        join public.pets p on p.id = a.pet_id
        join public.users u on u.id = a.user_id
    where a.id = p_alert_id;
$$;

grant execute on function public.get_alert_detail(uuid) to authenticated;

-- Postgrest caches the schema. Supabase reloads it on DDL, this makes it immediate.
notify pgrst, 'reload schema';
