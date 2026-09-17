-- The remaining RPCs the Epona client calls, plus the v_sighting_trail view.
-- Companion to 20260823000000_create_alert_function.sql.
--
-- Conventions that come from the client, not from taste:
--
--   * Optional arguments carry DEFAULTs. Postgrest resolves an RPC by the set of
--     argument names in the request body, and optional fields that are null do not
--     make it into that set -- so a call with a blank note or no type filter has to
--     resolve against the same function.
--
--   * Anything the client reads with decodeSingle() must still be SET-returning.
--     PostgrestResult.decodeSingle() is decodeList().first(), so the client is always
--     parsing a JSON array, even for a single row.
--
--   * Timestamps are timestamptz, never timestamp. DateMapper.toEpochMillis() parses
--     ISO_ZONED_DATE_TIME and silently falls back to System.currentTimeMillis() when
--     there is no offset -- a bare timestamp column turns every date into "now".
--
--   * ::text casts on type/status/species/size/gender so these work whether those
--     columns are text or Postgres enums.
--
--   * Output column names of a `returns table` function are in scope inside the body,
--     so every column reference below is table-qualified to avoid ambiguity errors.


-- ---------------------------------------------------------------------------
-- Replace, do not overload
--
-- Some of these already exist in this project with a different shape, which is what
-- "cannot change return type of existing function" means: create or replace cannot
-- alter a row type defined by OUT parameters. Dropping by name catches every existing
-- signature, including any whose argument types differ from the definitions below.
--
-- That last part is the real reason to do it this way. An old signature left in place
-- gives Postgrest two candidates for the same call, and it answers 300 "could not
-- choose the best candidate function" instead of running either one.
--
-- No CASCADE, deliberately. If something depends on one of these, the drop fails and
-- says so, rather than quietly taking the dependent object with it.
--
-- Dropping also discards the old grants, which is why every one is re-issued at the
-- bottom of this file.
-- ---------------------------------------------------------------------------
do $$
declare
    r record;
begin
    for r in
        select p.oid::regprocedure as signature
        from pg_proc p
            join pg_namespace n on n.oid = p.pronamespace
        where n.nspname = 'public'
          and p.proname in (
              'get_nearby_alerts',
              'get_alert_detail',
              'create_sighting',
              'update_user_location',
              'get_user_stats',
              'mark_notifications_read',
              'get_users_to_notify'
          )
    loop
        raise notice 'dropping %', r.signature;
        execute format('drop function %s', r.signature);
    end loop;
end
$$;

-- Same problem in view form: create or replace cannot rename or drop a view column.
drop view if exists public.v_sighting_trail;


-- ---------------------------------------------------------------------------
-- get_nearby_alerts -> List<NearbyAlertDto>   (AlertService.getNearbyAlerts)
-- ---------------------------------------------------------------------------
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


-- ---------------------------------------------------------------------------
-- get_alert_detail -> AlertDetailDto   (AlertService.getAlertDetail, decodeSingle)
-- ---------------------------------------------------------------------------
create or replace function public.get_alert_detail(p_alert_id uuid)
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
        u.id,
        u.display_name,
        u.avatar_url
    from public.alerts a
        join public.pets p on p.id = a.pet_id
        join public.users u on u.id = a.user_id
    where a.id = p_alert_id;
$$;


-- ---------------------------------------------------------------------------
-- create_sighting -> SightingDto   (SightingService.reportSighting, decodeSingle)
--
-- SECURITY DEFINER for one specific reason: the reporter is not the alert owner, so
-- an owner-scoped RLS policy on `alerts` would block the sighting_count bump. The
-- authorisation the definer bypasses is re-imposed by hand on the first line.
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
-- update_user_location -> void   (UserService.updateLocation)
-- ---------------------------------------------------------------------------
create or replace function public.update_user_location(
    p_user_id uuid,
    p_lat     double precision,
    p_lng     double precision
)
returns void
language sql
security invoker
set search_path = public, extensions
as $$
    update public.users
    set location   = st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography,
        updated_at = now()
    where users.id = p_user_id
      and users.id = auth.uid();
$$;


-- ---------------------------------------------------------------------------
-- get_user_stats -> UserStatsDto   (UserService.getUserStats, decodeSingle)
--
-- Scalar subqueries, so it always returns exactly one row -- decodeList().first()
-- would throw on an empty result.
-- ---------------------------------------------------------------------------
create or replace function public.get_user_stats(p_user_id uuid)
returns table (
    total_pets         integer,
    active_alerts      integer,
    resolved_alerts    integer,
    sightings_reported integer
)
language sql
stable
security invoker
set search_path = public
as $$
    select
        (select count(*) from public.pets pe
          where pe.owner_id = p_user_id)::int,
        (select count(*) from public.alerts al
          where al.user_id = p_user_id and al.status::text = 'active')::int,
        (select count(*) from public.alerts al
          where al.user_id = p_user_id and al.status::text = 'resolved')::int,
        (select count(*) from public.sightings si
          where si.reporter_id = p_user_id)::int;
$$;


-- ---------------------------------------------------------------------------
-- mark_notifications_read -> void   (NotificationService.markAllAsRead)
-- ---------------------------------------------------------------------------
create or replace function public.mark_notifications_read(p_user_id uuid)
returns void
language sql
security invoker
set search_path = public
as $$
    update public.notifications
    set is_read = true
    where notifications.user_id = p_user_id
      and notifications.user_id = auth.uid()
      and notifications.is_read = false;
$$;


-- ---------------------------------------------------------------------------
-- get_users_to_notify -> List<UserToNotifyDto>
--
-- SERVER SIDE ONLY. This reads other people's FCM tokens and locations, so it must
-- never be callable by `authenticated` -- any signed-in user could harvest the token
-- and approximate position of every neighbour. Supabase's default privileges grant
-- EXECUTE on new public functions to anon and authenticated, so the revoke below is
-- load-bearing, not decoration.
--
-- Call it from an Edge Function (or a trigger) with the service role. Note that
-- AlertService.getUsersToNotify in the Android client is currently dead code; wiring
-- it up from the app is exactly the leak this guards against.
-- ---------------------------------------------------------------------------
create or replace function public.get_users_to_notify(
    p_alert_lat       double precision,
    p_alert_lng       double precision,
    p_exclude_user_id uuid default null
)
returns table (
    user_id     uuid,
    fcm_token   text,
    distance_km double precision
)
language sql
stable
security definer
set search_path = public, extensions
as $$
    with origin as (
        select st_setsrid(st_makepoint(p_alert_lng, p_alert_lat), 4326)::geography as g
    )
    select
        u.id,
        u.fcm_token,
        st_distance(u.location, o.g) / 1000.0
    from public.users u
        cross join origin o
    where u.fcm_token is not null
      and u.location is not null
      and (p_exclude_user_id is null or u.id <> p_exclude_user_id)
      -- every user sets their own notification radius
      and st_dwithin(u.location, o.g, coalesce(u.alert_radius_km, 10) * 1000)
    order by st_distance(u.location, o.g);
$$;


-- ---------------------------------------------------------------------------
-- v_sighting_trail -> List<SightingTrailDto>   (SightingService.getSightingTrail)
--
-- security_invoker so the underlying RLS applies to whoever queries the view,
-- instead of to the view owner.
-- ---------------------------------------------------------------------------
create or replace view public.v_sighting_trail
with (security_invoker = true) as
select
    s.id                        as sighting_id,
    s.alert_id                  as alert_id,
    st_y(s.location::geometry)  as lat,
    st_x(s.location::geometry)  as lng,
    s.address                   as address,
    s.photo_urls                as sighting_photos,
    s.note                      as note,
    s.spotted_at                as spotted_at,
    s.created_at                as created_at,
    s.reporter_id               as reporter_id,
    u.display_name              as reporter_name,
    u.avatar_url                as reporter_avatar
from public.sightings s
    join public.users u on u.id = s.reporter_id;


-- ---------------------------------------------------------------------------
-- Privileges
-- ---------------------------------------------------------------------------
grant execute on function public.get_nearby_alerts(
    double precision, double precision, integer, text, integer, integer
) to authenticated;

grant execute on function public.get_alert_detail(uuid) to authenticated;

grant execute on function public.create_sighting(
    uuid, uuid, double precision, double precision, text, text[], text, timestamptz
) to authenticated;

grant execute on function public.update_user_location(
    uuid, double precision, double precision
) to authenticated;

grant execute on function public.get_user_stats(uuid) to authenticated;

grant execute on function public.mark_notifications_read(uuid) to authenticated;

grant select on public.v_sighting_trail to authenticated;

-- Server side only -- see the note above the function.
revoke execute on function public.get_users_to_notify(
    double precision, double precision, uuid
) from anon, authenticated;

grant execute on function public.get_users_to_notify(
    double precision, double precision, uuid
) to service_role;

notify pgrst, 'reload schema';
