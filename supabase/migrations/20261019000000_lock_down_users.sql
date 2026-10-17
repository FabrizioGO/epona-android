-- Lock down public.users and the surfaces that leaked it.
--
-- Found by auditing the live project:
--   * users_select_all was `for select to public using (true)`, so anyone holding
--     the publishable key (no sign-in) could read every user's email, phone,
--     fcm_token and home location.
--   * get_users_to_notify (fcm_token + distance for any lat/lng) was executable by
--     anon and authenticated. 20260823000100 revoked it from those two roles, but
--     functions are executable by PUBLIC by default, so that revoke was a no-op.
--     The same applies to the handle_* trigger functions.
--   * anon and authenticated held TRUNCATE/TRIGGER/REFERENCES on the core tables;
--     TRUNCATE is not subject to RLS.
--   * v_active_alerts and adoption_posts_with_owner ran as their owner (bypassing
--     RLS) and joined users directly.
--
-- Everything that needs another user's name or avatar now reads public_profiles,
-- which exposes id, display_name and avatar_url and nothing else.
--
-- Applied to the live project as migration `lock_down_users`.


-- 1. public_profiles: the only cross-user window onto users.
-- Deliberately a definer view (the default): it has to read rows RLS hides from the
-- caller, and the column list is what makes that safe. Supabase's
-- security_definer_view lint will flag it; that is expected.
create or replace view public.public_profiles as
select u.id, u.display_name, u.avatar_url from public.users u;

revoke all on public.public_profiles from public, anon, authenticated;
grant select on public.public_profiles to authenticated;

-- 2. Move every reader of other users' rows onto public_profiles.
-- Views become security_invoker so the base tables' RLS applies to the caller.
create or replace view public.v_sighting_trail
with (security_invoker = true) as
select
    s.id as sighting_id, s.alert_id as alert_id,
    st_y(s.location::geometry) as lat, st_x(s.location::geometry) as lng,
    s.address as address, s.photo_urls as sighting_photos, s.note as note,
    s.spotted_at as spotted_at, s.created_at as created_at, s.reporter_id as reporter_id,
    u.display_name as reporter_name, u.avatar_url as reporter_avatar
from public.sightings s
    join public.public_profiles u on u.id = s.reporter_id;

create or replace view public.v_active_alerts
with (security_invoker = true) as
select
    a.id as alert_id, a.type, a.status, a.last_seen_location, a.last_seen_address,
    a.last_seen_at, a.description as alert_description, a.reward, a.contact_phone,
    a.sighting_count, a.created_at as alert_created_at,
    p.id as pet_id, p.name as pet_name, p.species, p.breed, p.color, p.size, p.gender,
    p.photo_urls as pet_photos,
    u.id as owner_id, u.display_name as owner_name, u.avatar_url as owner_avatar
from public.alerts a
    join public.pets p on p.id = a.pet_id
    join public.public_profiles u on u.id = a.user_id
where a.status = 'active'::alert_status
order by a.created_at desc;

create or replace view public.adoption_posts_with_owner
with (security_invoker = true) as
select
    p.id, p.user_id, p.title, p.species, p.breed, p.pet_name, p.age_stage, p.size,
    p.gender, p.description, p.price, p.photo_urls, p.contact_phone, p.status,
    p.adopted_at, p.created_at, p.updated_at,
    u.display_name as owner_name, u.avatar_url as owner_avatar
from public.adoption_posts p
    join public.public_profiles u on u.id = p.user_id
where p.status = 'active' or p.user_id = auth.uid();

-- v_active_alerts is not queried by the app; the others are signed-in only.
revoke all on public.v_sighting_trail, public.v_active_alerts, public.adoption_posts_with_owner
    from public, anon, authenticated;
grant select on public.v_sighting_trail, public.v_active_alerts, public.adoption_posts_with_owner
    to authenticated;

-- Same bodies as before, with `public.users u` -> `public.public_profiles u`. Both
-- are security invoker, so they would otherwise lose every row once users is
-- own-row only.
create or replace function public.get_alert_detail(p_alert_id uuid)
returns table(
    alert_id uuid, type text, status text,
    last_seen_lat double precision, last_seen_lng double precision,
    last_seen_address text, last_seen_at timestamp with time zone,
    alert_description text, reward numeric, contact_phone text,
    sighting_count integer, created_at timestamp with time zone,
    resolved_at timestamp with time zone, pet_id uuid, pet_name text,
    species text, breed text, color text, size text, gender text,
    microchip_id text, pet_description text, pet_photos text[],
    pet_owner_id uuid, owner_id uuid, owner_name text, owner_avatar text
)
language sql
stable
set search_path to 'public', 'extensions'
as $function$
    select
        a.id, a.type::text, a.status::text,
        st_y(a.last_seen_location::geometry), st_x(a.last_seen_location::geometry),
        a.last_seen_address, a.last_seen_at, a.description, a.reward, a.contact_phone,
        a.sighting_count, a.created_at, a.resolved_at,
        p.id, p.name, p.species::text, p.breed, p.color, p.size::text, p.gender::text,
        p.microchip_id, p.description, p.photo_urls, p.owner_id,
        u.id, u.display_name, u.avatar_url
    from public.alerts a
        join public.pets p on p.id = a.pet_id
        join public.public_profiles u on u.id = a.user_id
    where a.id = p_alert_id;
$function$;

create or replace function public.get_nearby_alerts(
    p_lat double precision, p_lng double precision, p_radius_meters integer,
    p_type text default null::text, p_limit integer default 50, p_offset integer default 0
)
returns table(
    alert_id uuid, type text, found_custody text,
    last_seen_lat double precision, last_seen_lng double precision,
    last_seen_address text, last_seen_at timestamp with time zone,
    reward numeric, sighting_count integer, distance_meters double precision,
    pet_name text, species text, breed text, color text, pet_photos text[],
    owner_name text, created_at timestamp with time zone
)
language sql
stable
set search_path to 'public', 'extensions'
as $function$
    with origin as (
        select st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography as g
    )
    select
        a.id, a.type::text, a.found_custody,
        st_y(a.last_seen_location::geometry), st_x(a.last_seen_location::geometry),
        a.last_seen_address, a.last_seen_at, a.reward, a.sighting_count,
        st_distance(a.last_seen_location, o.g),
        p.name, p.species::text, p.breed, p.color, p.photo_urls,
        u.display_name, a.created_at
    from public.alerts a
        join public.pets p on p.id = a.pet_id
        join public.public_profiles u on u.id = a.user_id
        cross join origin o
    where a.status::text = 'active'
      and st_dwithin(a.last_seen_location, o.g, p_radius_meters)
      and (p_type is null or a.type::text = p_type)
    order by st_distance(a.last_seen_location, o.g)
    limit p_limit
    offset p_offset;
$function$;

-- 3. users: own row only.
drop policy if exists users_select_all on public.users;
drop policy if exists users_select_own on public.users;
create policy users_select_own on public.users
    for select to authenticated
    using (id = auth.uid());

-- 4. Functions that must not be callable through /rest/v1/rpc.
-- Revoke from PUBLIC as well: that is where the default EXECUTE comes from.
-- Triggers keep firing; Postgres does not check EXECUTE when a trigger runs.
-- get_users_to_notify stays available to service_role (edge functions).
revoke execute on function public.get_users_to_notify(double precision, double precision, uuid)
    from public, anon, authenticated;
grant execute on function public.get_users_to_notify(double precision, double precision, uuid)
    to service_role;

revoke execute on function public.handle_new_user()         from public, anon, authenticated;
revoke execute on function public.handle_updated_at()       from public, anon, authenticated;
revoke execute on function public.handle_new_sighting()     from public, anon, authenticated;
revoke execute on function public.handle_delete_sighting()  from public, anon, authenticated;
revoke execute on function public.handle_alert_resolved()   from public, anon, authenticated;

alter function public.handle_new_user()         set search_path = public, extensions;
alter function public.handle_updated_at()       set search_path = public, extensions;
alter function public.handle_new_sighting()     set search_path = public, extensions;
alter function public.handle_delete_sighting()  set search_path = public, extensions;
alter function public.handle_alert_resolved()   set search_path = public, extensions;

-- The RPCs the signed-in app calls stay executable by authenticated, but not by
-- anon: they reject a caller whose uid does not match, so anon can only error.
revoke execute on function public.create_sighting(
    uuid, uuid, double precision, double precision, text, text[], text, timestamptz
) from public, anon;
grant execute on function public.create_sighting(
    uuid, uuid, double precision, double precision, text, text[], text, timestamptz
) to authenticated;

revoke execute on function public.create_found_alert(
    uuid, text, double precision, double precision, text, text, text, text, text[], text,
    timestamptz, text, text
) from public, anon;
grant execute on function public.create_found_alert(
    uuid, text, double precision, double precision, text, text, text, text, text[], text,
    timestamptz, text, text
) to authenticated;

revoke execute on function public.create_found_alert(
    uuid, public.pet_species, double precision, double precision, text, text, text,
    public.pet_size, text, text[], text, timestamptz, text, text
) from public, anon;
grant execute on function public.create_found_alert(
    uuid, public.pet_species, double precision, double precision, text, text, text,
    public.pet_size, text, text[], text, timestamptz, text, text
) to authenticated;

-- 5. Table privileges.
-- RLS is the real gate for SELECT/INSERT/UPDATE/DELETE, but TRUNCATE ignores it, and
-- anon has no business with writes. anon keeps SELECT on the three tables whose
-- policies are intentionally world-readable (the public alert feed).
revoke all on public.users, public.notifications from anon;
revoke insert, update, delete, truncate, trigger, references
    on public.alerts, public.pets, public.sightings from anon;

revoke truncate, trigger, references
    on public.users, public.pets, public.alerts, public.sightings, public.notifications
    from authenticated;

notify pgrst, 'reload schema';
