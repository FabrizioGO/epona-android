-- User blocking (Google Play's user-generated-content policy asks for it next to
-- reporting).
--
-- A block is one row (blocker -> blocked). Visibility is mutual: if A blocked B or
-- B blocked A, neither sees the other's alerts, sightings or adoption posts, and
-- neither is notified about the other's activity. One symmetric helper,
-- is_blocked_between, is what every reader below calls.
--
-- The helper is security definer so that B, who cannot read A's user_blocks rows
-- (RLS: own rows only), is still filtered out of A's content. It returns only a
-- boolean about the caller's own relationship, never the other party's list.
--
-- get_alert_detail and get_nearby_alerts are redefined from the LIVE definitions
-- (pg_get_functiondef), not from 20260920000000_found_pets.sql, which the live
-- project no longer matches (see 20261019000000_lock_down_users.sql).


-- 1. Table
create table if not exists public.user_blocks (
    blocker_id uuid not null default auth.uid() references public.users (id) on delete cascade,
    blocked_id uuid not null references public.users (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (blocker_id, blocked_id),
    constraint user_blocks_not_self check (blocker_id <> blocked_id)
);

create index if not exists user_blocks_blocked_idx on public.user_blocks (blocked_id);

alter table public.user_blocks enable row level security;

revoke all on public.user_blocks from public, anon, authenticated;
grant select, delete on public.user_blocks to authenticated;
grant insert (blocked_id) on public.user_blocks to authenticated;

drop policy if exists user_blocks_select_own on public.user_blocks;
create policy user_blocks_select_own on public.user_blocks
    for select to authenticated using (blocker_id = auth.uid());

drop policy if exists user_blocks_insert_own on public.user_blocks;
create policy user_blocks_insert_own on public.user_blocks
    for insert to authenticated with check (blocker_id = auth.uid());

drop policy if exists user_blocks_delete_own on public.user_blocks;
create policy user_blocks_delete_own on public.user_blocks
    for delete to authenticated using (blocker_id = auth.uid());


-- 2. Cap, so one account cannot fill the table. Skipped for service_role (no uid).
create or replace function public.tg_user_blocks_limit()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() is not null
       and (select count(*) from public.user_blocks b where b.blocker_id = auth.uid()) >= 500 then
        raise exception 'Too many blocked users'
            using errcode = 'P0001', hint = 'rate_limited';
    end if;
    return new;
end;
$$;

revoke execute on function public.tg_user_blocks_limit() from public, anon, authenticated;

drop trigger if exists trg_user_blocks_limit on public.user_blocks;
create trigger trg_user_blocks_limit
    before insert on public.user_blocks
    for each row execute function public.tg_user_blocks_limit();


-- 3. Symmetric helper
create or replace function public.is_blocked_between(a uuid, b uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    -- Callable through /rest/v1/rpc by any signed-in user, so only answer about the
    -- caller's own relationships; otherwise it would reveal whether two other users
    -- blocked each other. auth.uid() is null for service_role/internal callers.
    select (auth.uid() is null or auth.uid() in (a, b))
       and exists (
        select 1 from public.user_blocks
        where (blocker_id = a and blocked_id = b)
           or (blocker_id = b and blocked_id = a)
    );
$$;

revoke execute on function public.is_blocked_between(uuid, uuid) from public, anon;
grant execute on function public.is_blocked_between(uuid, uuid) to authenticated, service_role;


-- 4. The blocked-users list: only the caller's own blocks, with public profile data.
create or replace view public.blocked_users_list
with (security_invoker = true) as
select b.blocked_id, p.display_name, p.avatar_url, b.created_at
from public.user_blocks b
    join public.public_profiles p on p.id = b.blocked_id
where b.blocker_id = auth.uid();

revoke all on public.blocked_users_list from public, anon, authenticated;
grant select on public.blocked_users_list to authenticated;


-- 5. Readers
-- owner_id is new, so the return type changes and the function must be dropped.
drop function if exists public.get_nearby_alerts(
    double precision, double precision, integer, text, integer, integer
);

create function public.get_nearby_alerts(
    p_lat double precision, p_lng double precision, p_radius_meters integer,
    p_type text default null::text, p_limit integer default 50, p_offset integer default 0
)
returns table(
    alert_id uuid, type text, found_custody text,
    last_seen_lat double precision, last_seen_lng double precision,
    last_seen_address text, last_seen_at timestamp with time zone,
    reward numeric, sighting_count integer, distance_meters double precision,
    pet_name text, species text, breed text, color text, pet_photos text[],
    owner_name text, created_at timestamp with time zone, owner_id uuid
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
        u.display_name, a.created_at, a.user_id
    from public.alerts a
        join public.pets p on p.id = a.pet_id
        join public.public_profiles u on u.id = a.user_id
        cross join origin o
    where a.status::text = 'active'
      and st_dwithin(a.last_seen_location, o.g, p_radius_meters)
      and (p_type is null or a.type::text = p_type)
      and not public.is_blocked_between(auth.uid(), a.user_id)
    order by st_distance(a.last_seen_location, o.g)
    limit p_limit
    offset p_offset;
$function$;

revoke execute on function public.get_nearby_alerts(
    double precision, double precision, integer, text, integer, integer
) from public, anon;
grant execute on function public.get_nearby_alerts(
    double precision, double precision, integer, text, integer, integer
) to authenticated;

-- Same signature as live: only the blocked filter is added.
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
    where a.id = p_alert_id
      and not public.is_blocked_between(auth.uid(), a.user_id);
$function$;

create or replace view public.v_sighting_trail
with (security_invoker = true) as
select
    s.id as sighting_id, s.alert_id as alert_id,
    st_y(s.location::geometry) as lat, st_x(s.location::geometry) as lng,
    s.address as address, s.photo_urls as sighting_photos, s.note as note,
    s.spotted_at as spotted_at, s.created_at as created_at, s.reporter_id as reporter_id,
    u.display_name as reporter_name, u.avatar_url as reporter_avatar
from public.sightings s
    join public.public_profiles u on u.id = s.reporter_id
where not public.is_blocked_between(auth.uid(), s.reporter_id);

create or replace view public.adoption_posts_with_owner
with (security_invoker = true) as
select
    p.id, p.user_id, p.title, p.species, p.breed, p.pet_name, p.age_stage, p.size,
    p.gender, p.description, p.price, p.photo_urls, p.contact_phone, p.status,
    p.adopted_at, p.created_at, p.updated_at,
    u.display_name as owner_name, u.avatar_url as owner_avatar
from public.adoption_posts p
    join public.public_profiles u on u.id = p.user_id
where (p.status = 'active' or p.user_id = auth.uid())
  and not public.is_blocked_between(auth.uid(), p.user_id);

-- create or replace view keeps existing grants, re-issued for clarity.
revoke all on public.v_sighting_trail, public.adoption_posts_with_owner
    from public, anon, authenticated;
grant select on public.v_sighting_trail, public.adoption_posts_with_owner to authenticated;


-- 6. Notifications: a blocked pair is never notified about each other.
-- notify_users gains an optional actor; the triggers pass whoever caused the event.
-- Dropping the old signature keeps 5-argument calls resolving to the new function.
drop function if exists public.notify_users(uuid[], uuid, text, text, jsonb);

create function public.notify_users(
    p_user_ids uuid[],
    p_alert_id uuid,
    p_type     text,
    p_key      text,
    p_args     jsonb default '{}'::jsonb,
    p_actor_id uuid  default null
)
returns integer
language plpgsql
volatile
security definer
set search_path = public
as $$
declare
    v_count integer := 0;
begin
    if p_user_ids is null or cardinality(p_user_ids) = 0 then
        return 0;
    end if;

    execute format(
        'insert into public.notifications
             (user_id, alert_id, type, title, body, is_read, created_at)
         select u.id, $2, $3::%s, c.title, c.body, false, now()
         from public.users u
             cross join lateral public.notification_copy(u.locale, $4, $5) c
         where u.id = any($1)
           and ($6 is null or not exists (
                select 1 from public.user_blocks b
                where (b.blocker_id = u.id and b.blocked_id = $6)
                   or (b.blocker_id = $6 and b.blocked_id = u.id)))',
        public.notifications_type_name()
    )
    using p_user_ids, p_alert_id, p_type, p_key, p_args, p_actor_id;

    get diagnostics v_count = row_count;
    return v_count;
end;
$$;

revoke all on function public.notify_users(uuid[], uuid, text, text, jsonb, uuid)
    from public, anon, authenticated;
grant execute on function public.notify_users(uuid[], uuid, text, text, jsonb, uuid)
    to service_role;

-- Triggers: same bodies as 20260916120000_notification_triggers.sql plus the actor.
create or replace function public.tg_sighting_notify_owner()
returns trigger
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    v_owner_id uuid;
    v_pet_name text;
begin
    select a.user_id, p.name
      into v_owner_id, v_pet_name
      from public.alerts a
          left join public.pets p on p.id = a.pet_id
     where a.id = new.alert_id;

    if v_owner_id is null or v_owner_id = new.reporter_id then
        return null;
    end if;

    perform public.notify_users(
        array[v_owner_id], new.alert_id, 'sighting', 'sighting',
        jsonb_build_object('pet_name', v_pet_name, 'address', left(new.address, 80)),
        new.reporter_id
    );

    return null;
exception
    when others then
        raise warning 'epona: sighting notification failed for sighting % (%)',
            new.id, sqlerrm;
        return null;
end;
$$;

create or replace function public.tg_alert_notify_nearby()
returns trigger
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    v_pet_name text;
    v_key      text;
    v_ids      uuid[];
begin
    if new.status::text <> 'active' or new.last_seen_location is null then
        return null;
    end if;

    select p.name into v_pet_name from public.pets p where p.id = new.pet_id;

    v_key := case new.type::text
                 when 'found' then 'new_alert_found'
                 else              'new_alert_lost'
             end;

    select array_agg(x.id) into v_ids
    from (
        select u.id
        from public.users u
        where u.id <> new.user_id
          and u.location is not null
          and st_dwithin(u.location, new.last_seen_location,
                         coalesce(u.alert_radius_km, 10) * 1000)
        order by st_distance(u.location, new.last_seen_location)
        limit 500
    ) x;

    perform public.notify_users(
        v_ids, new.id, 'new_alert', v_key,
        jsonb_build_object('pet_name', v_pet_name, 'address', left(new.last_seen_address, 80)),
        new.user_id
    );

    return null;
exception
    when others then
        raise warning 'epona: nearby fan-out failed for alert % (%)', new.id, sqlerrm;
        return null;
end;
$$;

create or replace function public.tg_alert_notify_resolved()
returns trigger
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    v_pet_name text;
    v_ids      uuid[];
begin
    select p.name into v_pet_name from public.pets p where p.id = new.pet_id;

    select array_agg(distinct s.reporter_id) into v_ids
    from public.sightings s
    where s.alert_id = new.id
      and s.reporter_id is not null
      and s.reporter_id <> new.user_id;

    perform public.notify_users(
        v_ids, new.id, 'resolved', 'resolved',
        jsonb_build_object('pet_name', v_pet_name),
        new.user_id
    );

    return null;
exception
    when others then
        raise warning 'epona: resolved fan-out failed for alert % (%)', new.id, sqlerrm;
        return null;
end;
$$;

revoke execute on function public.tg_sighting_notify_owner(), public.tg_alert_notify_nearby(),
    public.tg_alert_notify_resolved() from public, anon, authenticated;

notify pgrst, 'reload schema';
