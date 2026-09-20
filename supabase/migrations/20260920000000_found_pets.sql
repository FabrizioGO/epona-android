-- Found pets: an alert for an animal nobody on Epona owns.
--
-- Until now `alerts.pet_id` pointed at a row in `pets` that always had an owner, so the
-- only way to post "I found a dog" was to register the stray as your own pet -- name,
-- gender, age and microchip included, none of which a finder knows. This migration makes
-- an ownerless, nameless pet representable, and adds the one RPC that creates it.
--
-- It also records where the animal is now, which is not decoration: a pet the finder took
-- home cannot be spotted by anyone else, so sightings on it are meaningless and the post
-- has to say "contact the finder" instead. See create_sighting below, which enforces that.
--
-- Conventions inherited from 20260823000100_rpc_functions.sql, all of them driven by the
-- Kotlin client rather than by taste:
--
--   * Optional arguments carry DEFAULTs. Postgrest resolves an RPC by the set of argument
--     names in the request body, and a null optional field never makes it into that set.
--   * Anything read with decodeSingle() must still be SET-returning -- decodeSingle() is
--     decodeList().first(), so the client is always parsing a JSON array.
--   * Timestamps are timestamptz, never timestamp.
--   * ::text casts on type/status/species/size/gender so these work whether those columns
--     are text or Postgres enums.
--   * Output column names of a `returns table` function are in scope inside the body, so
--     every column reference is table-qualified.


-- ---------------------------------------------------------------------------
-- 1. A pet may have no owner and no name
--
-- Both are what "found" means. Nothing else has to change to keep them out of the way:
-- PetService.getMyPets and PetDao.observeMyPets both filter `owner_id = :ownerId`, and
-- get_user_stats counts `pets where owner_id = p_user_id` -- a NULL matches neither, so an
-- ownerless pet belongs to nobody's My Pets list and counts toward nobody's total.
--
-- notification_copy already coalesces a null pet_name to 'A pet' / 'Una mascota', and its
-- new_alert_found copy never names the pet at all, so the push path needs no change.
-- ---------------------------------------------------------------------------
alter table public.pets alter column owner_id drop not null;
alter table public.pets alter column name     drop not null;


-- ---------------------------------------------------------------------------
-- 2. Where the found pet is now
--
-- NULL for a lost alert, which has no custody. Plain text with a CHECK rather than a new
-- enum type: the client already casts every enum-backed column ::text on the way out, so
-- an enum buys nothing here and costs an ALTER TYPE every time the set grows.
-- ---------------------------------------------------------------------------
alter table public.alerts add column if not exists found_custody text;

do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conrelid = 'public.alerts'::regclass
          and conname  = 'alerts_found_custody_valid'
    ) then
        alter table public.alerts
            add constraint alerts_found_custody_valid
            check (found_custody is null or found_custody in ('with_finder', 'at_location'));
    end if;
end
$$;

comment on column public.alerts.found_custody is
    'For type = ''found'' only. with_finder = the finder took the animal home or to a vet '
    'or shelter, so nobody else can sight it and create_sighting refuses; at_location = '
    'they left it where they saw it, so sightings build the usual trail. NULL when lost.';


-- ---------------------------------------------------------------------------
-- 3. create_found_alert -> AlertDto   (AlertService.createFoundAlert, decodeSingle)
--
-- One transaction for the ownerless pet and the alert that points at it. Doing this
-- client-side as two calls would leave an orphan pet behind whenever the second failed,
-- and would need an RLS insert policy permitting `owner_id is null` -- which is exactly
-- the hole a security definer function with an explicit auth check avoids.
--
-- SECURITY DEFINER for the same reason create_sighting is: the writer is not the row's
-- owner, because the row has no owner. The authorisation that bypasses is re-imposed by
-- hand on the first line of the body.
--
-- p_species and p_size are declared %TYPE rather than text. The base DDL for `pets` is not
-- in this repo (see the note at the top of 20260916120000_notification_triggers.sql), so
-- whether those columns are text or enums is not knowable from here -- and assigning a
-- text variable to an enum column is exactly the error create_alert documents for
-- p_type::alert_type. %TYPE resolves to whatever the column actually is, and Postgrest
-- casts the incoming JSON string to the parameter type either way.
-- ---------------------------------------------------------------------------
create or replace function public.create_found_alert(
    p_user_id           uuid,
    p_species           public.pets.species%type,
    p_lat               double precision,
    p_lng               double precision,
    p_custody           text,
    p_breed             text        default null,
    p_color             text        default null,
    p_size              public.pets.size%type default null,
    p_pet_description   text        default null,
    p_photo_urls        text[]      default '{}',
    p_address           text        default null,
    p_last_seen_at      timestamptz default null,
    p_alert_description text        default null,
    p_contact_phone     text        default null
)
returns setof public.alerts
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    v_pet_id uuid;
    v_alert  public.alerts;
begin
    if p_user_id is distinct from auth.uid() then
        raise exception 'A found alert can only be posted as the signed-in user'
            using errcode = '42501';
    end if;

    -- No DEFAULT on p_custody, and validated rather than coalesced. Guessing wrong either
    -- strands an animal nobody is allowed to report seeing, or tells a neighbourhood to
    -- watch out for a dog that is asleep in someone's flat.
    if p_custody is null or p_custody not in ('with_finder', 'at_location') then
        raise exception 'p_custody must be with_finder or at_location, got %', p_custody
            using errcode = '22023';
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
        null,   -- the whole point: this animal is nobody's pet on Epona
        null,   -- and a finder does not know its name
        p_species,
        p_breed,
        p_color,
        p_size,
        p_pet_description,
        coalesce(p_photo_urls, '{}')
    )
    returning id into v_pet_id;

    insert into public.alerts (
        pet_id,
        user_id,
        type,
        status,
        found_custody,
        last_seen_location,
        last_seen_address,
        last_seen_at,
        description,
        contact_phone
    )
    values (
        v_pet_id,
        p_user_id,
        -- Bare literals, not variables, so they coerce to alert_type / alert_status on
        -- their own -- the explicit cast create_alert needs is only for a text variable.
        'found',
        'active',
        p_custody,
        -- ST_MakePoint is (x, y) -- longitude first, then latitude.
        st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography,
        p_address,
        coalesce(p_last_seen_at, now()),
        p_alert_description,
        p_contact_phone
    )
    returning * into v_alert;

    return next v_alert;
end;
$$;

-- Postgres 14+ lets the argument list be omitted when the name is unique in the schema,
-- which matters here because two of the argument types are whatever %TYPE resolved to.
grant execute on function public.create_found_alert to authenticated;


-- ---------------------------------------------------------------------------
-- 4. create_sighting -- refuse one on a pet already in the finder's care
--
-- Replaces the definition in 20260823000100_rpc_functions.sql; identical signature, so
-- create or replace is enough and the existing grant survives.
--
-- The guard has to live in the function. create_sighting is security definer precisely so
-- a non-owner can bump alerts.sighting_count, which means an RLS policy on `alerts` is not
-- consulted and could not enforce this. The client hides the button, but a stale screen, a
-- deep link or a replayed request all arrive here -- this is what makes it true.
-- ---------------------------------------------------------------------------
create or replace function public.create_sighting(
    p_alert_id    uuid,
    p_reporter_id uuid,
    p_lat         double precision,
    p_lng         double precision,
    p_address     text        default null,
    p_photo_urls  text[]      default '{}',
    p_note        text        default null,
    p_spotted_at  timestamptz default null
)
returns setof public.sightings
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    v_sighting public.sightings;
begin
    if p_reporter_id is distinct from auth.uid() then
        raise exception 'A sighting can only be reported as the signed-in user'
            using errcode = '42501';
    end if;

    if exists (
        select 1
        from public.alerts a
        where a.id = p_alert_id
          and a.found_custody = 'with_finder'
    ) then
        raise exception 'This pet is already in the finder''s care; contact them instead'
            using errcode = '22023';
    end if;

    insert into public.sightings (
        alert_id,
        reporter_id,
        location,
        address,
        photo_urls,
        note,
        spotted_at
    )
    values (
        p_alert_id,
        p_reporter_id,
        st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography,
        p_address,
        coalesce(p_photo_urls, '{}'),
        p_note,
        coalesce(p_spotted_at, now())
    )
    returning * into v_sighting;

    -- alerts.sighting_count is read straight off the row by the feed, the map preview
    -- and the detail screen, so nothing else keeps it honest.
    update public.alerts
    set sighting_count = coalesce(sighting_count, 0) + 1,
        updated_at     = now()
    where id = p_alert_id;

    return next v_sighting;
end;
$$;


-- ---------------------------------------------------------------------------
-- 5. Expose the two new facts to the read paths
--
-- create or replace cannot add a column to a `returns table` function ("cannot change
-- return type of existing function"), so both are dropped by signature first. Dropping
-- also discards their grants, which is why each is re-issued below.
--
-- get_alert_detail gains two columns:
--
--   * found_custody -- so the detail screen can swap its actions without a second call.
--   * pet_owner_id  -- distinct from owner_id, which is the *alert poster*. AlertMapper
--     was assigning owner_id to Pet.ownerId as well as Alert.userId; with found pets that
--     cached the stray as owned by whoever opened the alert, and PetDao.observeMyPets
--     would then have shown it in their My Pets list.
-- ---------------------------------------------------------------------------
drop function if exists public.get_alert_detail(uuid);

create or replace function public.get_alert_detail(p_alert_id uuid)
returns table (
    alert_id          uuid,
    type              text,
    status            text,
    found_custody     text,
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
        a.found_custody,
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


-- get_nearby_alerts gains found_custody so the feed and the map can mark a found pet as
-- already in safe hands, and so the create-alert wizard's match step can say "contact the
-- finder" rather than "report a sighting" on a row that will not accept one.
drop function if exists public.get_nearby_alerts(
    double precision, double precision, integer, text, integer, integer
);

create or replace function public.get_nearby_alerts(
    p_lat           double precision,
    p_lng           double precision,
    p_radius_meters integer,
    p_type          text    default null,
    p_limit         integer default 50,
    p_offset        integer default 0
)
returns table (
    alert_id          uuid,
    type              text,
    found_custody     text,
    last_seen_lat     double precision,
    last_seen_lng     double precision,
    last_seen_address text,
    last_seen_at      timestamptz,
    reward            numeric,
    sighting_count    integer,
    distance_meters   double precision,
    pet_name          text,
    species           text,
    breed             text,
    color             text,
    pet_photos        text[],
    owner_name        text,
    created_at        timestamptz
)
language sql
stable
security invoker
set search_path = public, extensions
as $$
    with origin as (
        select st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography as g
    )
    select
        a.id,
        a.type::text,
        a.found_custody,
        st_y(a.last_seen_location::geometry),
        st_x(a.last_seen_location::geometry),
        a.last_seen_address,
        a.last_seen_at,
        a.reward,
        a.sighting_count,
        st_distance(a.last_seen_location, o.g),
        p.name,
        p.species::text,
        p.breed,
        p.color,
        p.photo_urls,
        u.display_name,
        a.created_at
    from public.alerts a
        join public.pets p on p.id = a.pet_id
        join public.users u on u.id = a.user_id
        cross join origin o
    where a.status::text = 'active'
      and st_dwithin(a.last_seen_location, o.g, p_radius_meters)
      and (p_type is null or a.type::text = p_type)
    order by st_distance(a.last_seen_location, o.g)
    limit p_limit
    offset p_offset;
$$;

grant execute on function public.get_nearby_alerts(
    double precision, double precision, integer, text, integer, integer
) to authenticated;


-- Postgrest caches the schema. Supabase reloads it on DDL, this makes it immediate.
notify pgrst, 'reload schema';
