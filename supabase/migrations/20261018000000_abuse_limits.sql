-- Abuse limits on the two writes that fan out to other people.
--
-- Every alert notifies up to 500 nearby users (tg_alert_notify_nearby in
-- 20260916120000_notification_triggers.sql), and every sighting notifies the
-- alert's owner. Nothing so far stopped one signed-in account from scripting
-- thousands of either against the REST API with the anon key from the APK.
--
-- The limits are BEFORE INSERT triggers rather than checks inside create_alert /
-- create_found_alert / create_sighting, so they also hold for any direct table
-- insert an RLS policy might allow, and for any RPC added later. They only apply
-- when auth.uid() is set: the service role (edge functions, the dashboard, manual
-- fixes) has no uid and is never limited.
--
-- The trigger functions are security definer so the count sees every row the user
-- has written, not just the ones RLS lets them read back.


-- ---------------------------------------------------------------------------
-- 1. Alerts: at most 5 per user per rolling 24 hours (lost and found together).
--
-- A real person rarely loses more than one pet in a day; five leaves room for a
-- typo'd post that gets deleted and reposted.
-- ---------------------------------------------------------------------------
create or replace function public.tg_alerts_rate_limit()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_uid uuid := auth.uid();
begin
    if v_uid is null then
        return new;
    end if;

    if (
        select count(*)
        from public.alerts a
        where a.user_id = v_uid
          and a.created_at > now() - interval '24 hours'
    ) >= 5 then
        raise exception 'Too many alerts posted in the last 24 hours. Try again later.'
            using errcode = 'P0001', hint = 'rate_limited';
    end if;

    return new;
end;
$$;

drop trigger if exists trg_alerts_rate_limit on public.alerts;
create trigger trg_alerts_rate_limit
    before insert on public.alerts
    for each row execute function public.tg_alerts_rate_limit();


-- ---------------------------------------------------------------------------
-- 2. Sightings: only on active alerts, and at most 20 per user per rolling 24 hours.
--
-- The active check used to be the client's job (it hides the button on a resolved
-- alert), but a stale screen or a replayed request still reached create_sighting and
-- pinged the owner about a pet they had already found.
-- ---------------------------------------------------------------------------
create or replace function public.tg_sightings_guard()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_uid uuid := auth.uid();
begin
    if not exists (
        select 1
        from public.alerts a
        where a.id = new.alert_id
          and a.status = 'active'
    ) then
        raise exception 'This alert is no longer active'
            using errcode = '22023';
    end if;

    if v_uid is null then
        return new;
    end if;

    if (
        select count(*)
        from public.sightings s
        where s.reporter_id = v_uid
          and s.created_at > now() - interval '24 hours'
    ) >= 20 then
        raise exception 'Too many sightings reported in the last 24 hours. Try again later.'
            using errcode = 'P0001', hint = 'rate_limited';
    end if;

    return new;
end;
$$;

drop trigger if exists trg_sightings_guard on public.sightings;
create trigger trg_sightings_guard
    before insert on public.sightings
    for each row execute function public.tg_sightings_guard();

-- Trigger functions are never called directly; keep them off the RPC surface.
revoke all on function public.tg_alerts_rate_limit() from public, anon, authenticated;
revoke all on function public.tg_sightings_guard()   from public, anon, authenticated;


-- ---------------------------------------------------------------------------
-- 3. Drop the first create_found_alert.
--
-- 20260920000000_found_pets.sql added p_custody, which changed the argument list,
-- so `create or replace` created a second overload instead of replacing this one.
-- The old one is still security definer, still granted to authenticated, and skips
-- the custody validation.
-- ---------------------------------------------------------------------------
drop function if exists public.create_found_alert(
    uuid, text, double precision, double precision,
    text, text, text, text, text[], text, timestamptz, text, text
);

notify pgrst, 'reload schema';
