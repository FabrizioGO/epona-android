-- Notification rows, and the push that follows them.
--
-- NotificationsScreen has always been empty because nothing in this project ever
-- writes to public.notifications. The Room -> UI path works; the table just never
-- receives a row. This file makes Postgres the author of every notification.
--
-- Postgres, specifically, and not the client or an Edge Function, for three reasons:
--
--   1. The recipient is almost never the caller. A sighting notifies the alert
--      owner; a new alert notifies every neighbour inside their own radius. A client
--      that wrote those rows would need INSERT rights on other people's
--      notifications, which is the same as write access to everyone's inbox.
--
--   2. The events are already transactions here. create_sighting inserts and bumps
--      sighting_count in one statement; AlertService.resolveAlert is a plain
--      PostgREST UPDATE with no RPC to hook. A trigger sees all three without the
--      client cooperating, and cannot be skipped by an old app version.
--
--   3. There is exactly one writer, so there is exactly one place where the copy
--      lives. See notification_copy below.
--
-- Localisation is server-side for one blunt reason: the FCM tray text is rendered
-- by the server, not by the app. If the title/body were English constants, the
-- system-tray push would be English for a Spanish user no matter what values-es
-- contains. So the recipient's language has to be a column, and the copy has to be
-- a function of it. users.locale is written by the Android client on login
-- (Locale.getDefault().language -> 'en', 'es').
--
-- Everything here is re-runnable. Triggers are dropped before their functions,
-- because "drop function" without CASCADE refuses while a trigger depends on it,
-- and CASCADE is not something this directory does.
--
-- WHAT THIS FILE ASSUMES, having no CREATE TABLE anywhere in the repo to read --
-- see supabase/preflight_notifications_check.sql, run it first:
--
--   * notifications(user_id, alert_id, type, title, body, is_read, created_at).
--     `type` may be text or an enum -- notifications_type_name() resolves that at
--     runtime rather than guessing.
--   * users(id, location geography, alert_radius_km, fcm_token).
--   * alerts(id, pet_id, user_id, type, status, last_seen_location,
--     last_seen_address, resolved_at, updated_at).
--   * sightings(alert_id, reporter_id, address).
--   * pets(id, name).
--
-- Nothing below relies on a DEFAULT existing on notifications.is_read or
-- .created_at; both are written explicitly.


-- ---------------------------------------------------------------------------
-- 1. users.locale
--
-- Added with a DEFAULT so PG11+ fills existing rows without a table rewrite, then
-- backfilled explicitly for rows that predate this and would otherwise be null.
-- The check is NOT VALID on purpose: it constrains everything written from now on
-- without failing this migration over one pre-existing oddity.
-- ---------------------------------------------------------------------------
alter table public.users add column if not exists locale text;
alter table public.users alter column locale set default 'en';

update public.users set locale = 'en' where locale is null;

alter table public.users drop constraint if exists users_locale_check;
alter table public.users add constraint users_locale_check
    check (locale is null or locale ~ '^[a-z]{2,3}([-_][A-Za-z0-9]{2,8})*$')
    not valid;

comment on column public.users.locale is
    'BCP-47-ish language tag written by the Android client on login. Only the first '
    'two characters are read (see notification_copy); es-MX and es both mean Spanish.';


-- ---------------------------------------------------------------------------
-- 2. Drop triggers, then functions
--
-- Same reasoning as 20260823000100: create or replace cannot change a row type
-- defined by OUT parameters, and an old signature left behind gives PostgREST two
-- candidates for one call. Triggers go first because a function with a dependent
-- trigger cannot be dropped without CASCADE.
-- ---------------------------------------------------------------------------
drop trigger if exists trg_sightings_notify_owner   on public.sightings;
drop trigger if exists trg_alerts_notify_nearby     on public.alerts;
drop trigger if exists trg_alerts_notify_resolved   on public.alerts;
drop trigger if exists trg_alerts_stamp_resolved_at on public.alerts;
drop trigger if exists trg_notifications_push       on public.notifications;

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
              'notification_copy',
              'notifications_type_name',
              'notify_user',
              'notify_users',
              'epona_secret',
              'clear_fcm_tokens',
              'tg_sighting_notify_owner',
              'tg_alert_notify_nearby',
              'tg_alert_notify_resolved',
              'tg_alert_stamp_resolved_at',
              'tg_notifications_push'
          )
    loop
        raise notice 'dropping %', r.signature;
        execute format('drop function %s', r.signature);
    end loop;
end
$$;


-- ---------------------------------------------------------------------------
-- 3. notification_copy -- the entire user-visible vocabulary of this feature
--
-- One function, one language block per locale, one branch per message key. Adding
-- Portuguese is adding an `elsif v_lang = 'pt'` block and nothing else; no call
-- site changes, no new columns.
--
-- Message keys are deliberately finer-grained than notification types: a lost-pet
-- alert and a found-pet alert are both type 'new_alert' to the client, but they
-- are different sentences. The type is what NotificationIcon switches on; the key
-- is what the reader sees.
--
-- Spanish copy avoids adjectives that agree with the pet's gender ("se perdio",
-- not "esta perdido"), because pets.gender is not consulted here and getting it
-- wrong reads as careless in a message about someone's missing dog. Tone follows
-- values-es/strings.xml.
--
-- format() rather than || throughout: a pet called "100%" turns a concatenated
-- format string into a runtime error.
-- ---------------------------------------------------------------------------
create or replace function public.notification_copy(
    p_locale text,
    p_key    text,
    p_args   jsonb default '{}'::jsonb,
    out title text,
    out body  text
)
language plpgsql
immutable
set search_path = public
as $$
declare
    v_lang text;
    v_pet  text;   -- the name as given, or null
    v_own  text;   -- owner-facing fallback  ("your pet" / "tu mascota")
    v_any  text;   -- stranger-facing fallback ("A pet" / "Una mascota")
    v_addr text;
begin
    -- 'es-MX', 'es_AR', 'ES' and 'es' all select the same catalogue.
    v_lang := lower(left(coalesce(nullif(trim(p_locale), ''), 'en'), 2));
    v_pet  := nullif(trim(coalesce(p_args ->> 'pet_name', '')), '');
    v_addr := nullif(trim(coalesce(p_args ->> 'address',  '')), '');

    if v_lang = 'es' then
        v_own := coalesce(v_pet, 'tu mascota');
        v_any := coalesce(v_pet, 'Una mascota');

        case p_key
            when 'sighting' then
                title := coalesce(format('Nuevo avistamiento de %s', v_pet),
                                  'Nuevo avistamiento');
                body  := case when v_addr is null
                    then format('Alguien reportó haber visto a %s. Toca para ver el rastro.', v_own)
                    else format('Alguien reportó haber visto a %s cerca de %s. Toca para ver el rastro.', v_own, v_addr)
                end;

            when 'new_alert_lost' then
                title := 'Mascota perdida cerca de ti';
                body  := case when v_addr is null
                    then format('%s se perdió cerca de ti. Mantente atento.', v_any)
                    else format('%s se perdió cerca de %s. Mantente atento.', v_any, v_addr)
                end;

            when 'new_alert_found' then
                title := 'Mascota encontrada cerca de ti';
                body  := case when v_addr is null
                    then 'Alguien encontró una mascota cerca de ti. Échale un vistazo, quizá sepas de quién es.'
                    else format('Alguien encontró una mascota cerca de %s. Échale un vistazo, quizá sepas de quién es.', v_addr)
                end;

            when 'resolved' then
                title := coalesce(format('%s ya está en casa', v_pet), '¡Buenas noticias!');
                body  := 'La alerta en la que reportaste un avistamiento fue resuelta. ¡Gracias por ayudar!';

            else
                -- Matches notification_default_title in values-es/strings.xml.
                title := 'Alerta de Epona';
                body  := '';
        end case;

    else
        v_own := coalesce(v_pet, 'your pet');
        v_any := coalesce(v_pet, 'A pet');

        case p_key
            when 'sighting' then
                title := coalesce(format('New sighting of %s', v_pet), 'New sighting');
                body  := case when v_addr is null
                    then format('Someone reported seeing %s. Tap to see the trail.', v_own)
                    else format('Someone reported seeing %s near %s. Tap to see the trail.', v_own, v_addr)
                end;

            when 'new_alert_lost' then
                title := 'Lost pet near you';
                body  := case when v_addr is null
                    then format('%s went missing nearby. Keep an eye out.', v_any)
                    else format('%s went missing near %s. Keep an eye out.', v_any, v_addr)
                end;

            when 'new_alert_found' then
                title := 'Found pet near you';
                body  := case when v_addr is null
                    then 'Someone found a pet nearby. Take a look, you might know who they belong to.'
                    else format('Someone found a pet near %s. Take a look, you might know who they belong to.', v_addr)
                end;

            when 'resolved' then
                title := coalesce(format('%s is back home', v_pet), 'Good news!');
                body  := 'The alert you reported a sighting on was resolved. Thank you for helping.';

            else
                title := 'Epona Alert';
                body  := '';
        end case;
    end if;
end;
$$;


-- ---------------------------------------------------------------------------
-- 4. notifications_type_name
--
-- This project has no CREATE TABLE to read, so nobody here knows whether
-- notifications.type is text or an enum. It matters: a bare literal coerces to
-- either, but a text *variable* assigned to an enum column is the "column is of
-- type X but expression is of type text" error that create_alert already documents.
--
-- Rather than guess, ask the catalogue once and build the cast into the statement.
-- Returns 'text', or 'notification_type', or whatever it actually is.
-- ---------------------------------------------------------------------------
create or replace function public.notifications_type_name()
returns text
language sql
stable
set search_path = public, pg_catalog
as $$
    select format_type(a.atttypid, a.atttypmod)
    from pg_attribute a
    where a.attrelid = 'public.notifications'::regclass
      and a.attname  = 'type'
      and a.attnum > 0
      and not a.attisdropped;
$$;


-- ---------------------------------------------------------------------------
-- 5. notify_user / notify_users
--
-- SECURITY DEFINER, and that is the whole point: the RLS in section 6 gives
-- nobody INSERT on notifications. These functions run as the table owner, which
-- is how a row lands in someone else's inbox without any client ever holding the
-- privilege to put it there.
--
-- notify_users is the one the triggers call. It is set-based rather than a loop
-- because a new alert can reach every neighbour at once, and N round trips through
-- plpgsql inside the caller's transaction is the difference between a fast alert
-- insert and a slow one. The lateral join renders each recipient's copy in each
-- recipient's language in the same statement that inserts the rows.
--
-- notify_user (singular, pre-rendered title/body) exists for the 'message' type,
-- which the schema permits and which has no trigger: there is no chat feature in
-- Epona. It is the manual/system escape hatch, service_role only.
--
-- is_read and created_at are written explicitly. Assuming DEFAULTs on columns this
-- file cannot see is exactly the kind of guess that fails in production only.
-- ---------------------------------------------------------------------------
create or replace function public.notify_user(
    p_user_id  uuid,
    p_alert_id uuid,
    p_type     text,
    p_title    text,
    p_body     text
)
returns uuid
language plpgsql
volatile
security definer
set search_path = public
as $$
declare
    v_id uuid;
begin
    if p_user_id is null then
        return null;
    end if;

    execute format(
        'insert into public.notifications
             (user_id, alert_id, type, title, body, is_read, created_at)
         values ($1, $2, $3::%s, $4, $5, false, now())
         returning id',
        public.notifications_type_name()
    )
    into v_id
    using p_user_id, p_alert_id, p_type,
          coalesce(p_title, ''), coalesce(p_body, '');

    return v_id;
end;
$$;

create or replace function public.notify_users(
    p_user_ids uuid[],
    p_alert_id uuid,
    p_type     text,
    p_key      text,
    p_args     jsonb default '{}'::jsonb
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

    -- One statement, so the push trigger in section 8 fires once for the whole
    -- fan-out instead of once per recipient.
    execute format(
        'insert into public.notifications
             (user_id, alert_id, type, title, body, is_read, created_at)
         select u.id, $2, $3::%s, c.title, c.body, false, now()
         from public.users u
             cross join lateral public.notification_copy(u.locale, $4, $5) c
         where u.id = any($1)',
        public.notifications_type_name()
    )
    using p_user_ids, p_alert_id, p_type, p_key, p_args;

    get diagnostics v_count = row_count;
    return v_count;
end;
$$;


-- ---------------------------------------------------------------------------
-- 6. The triggers
--
-- Every one of them swallows its own errors. A notification is a courtesy; an
-- alert and a sighting are the product. If notify_users throws -- a constraint
-- nobody here can see, a null this file did not anticipate -- the user's sighting
-- must still be saved. The exception block turns a failure into a WARNING in the
-- Postgres log and returns, instead of rolling back create_sighting.
--
-- All three return null: the return value of an AFTER ... FOR EACH ROW trigger is
-- discarded.
-- ---------------------------------------------------------------------------

-- 6a. sighting -> the alert owner
--
-- Skips when the reporter is the owner. Owners do walk their own trail and add
-- sightings to it; notifying someone about their own action is how an inbox
-- becomes noise.
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
        array[v_owner_id],
        new.alert_id,
        'sighting',
        'sighting',
        jsonb_build_object(
            'pet_name', v_pet_name,
            -- Addresses from the geocoder run long; FCM caps a message at 4 KB and
            -- a tray line is unreadable past a phrase anyway.
            'address',  left(new.address, 80)
        )
    );

    return null;
exception
    when others then
        raise warning 'epona: sighting notification failed for sighting % (%)',
            new.id, sqlerrm;
        return null;
end;
$$;

create trigger trg_sightings_notify_owner
after insert on public.sightings
for each row
execute function public.tg_sighting_notify_owner();


-- 6b. new alert -> everyone nearby
--
-- The radius belongs to the reader, not the alert: st_dwithin compares against
-- each user's own alert_radius_km, defaulting to the same 10 km get_users_to_notify
-- uses. Unlike get_users_to_notify this does NOT require fcm_token -- that filter
-- is correct for a push-only fan-out and wrong here, because a user with
-- notifications denied still opens NotificationsScreen and expects to find things
-- in it.
--
-- Both alert types fan out. A found-pet alert two streets away is exactly as
-- relevant to the person whose dog is missing; only the sentence differs.
--
-- The limit is a blast radius, not a feature: see the note in the plan about
-- write amplification. Ordering by distance means that if it ever truncates, it
-- truncates the people furthest away.
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
        v_ids,
        new.id,
        'new_alert',
        v_key,
        jsonb_build_object(
            'pet_name', v_pet_name,
            'address',  left(new.last_seen_address, 80)
        )
    );

    return null;
exception
    when others then
        raise warning 'epona: nearby fan-out failed for alert % (%)', new.id, sqlerrm;
        return null;
end;
$$;

create trigger trg_alerts_notify_nearby
after insert on public.alerts
for each row
execute function public.tg_alert_notify_nearby();


-- 6c. resolved -> everyone who reported a sighting on it
--
-- An AFTER UPDATE trigger rather than anything inside an RPC, because
-- AlertService.resolveAlert is a plain PostgREST UPDATE on the table. There is no
-- function to extend.
--
-- The WHEN clause is the transition guard. `update of status` means the trigger is
-- not even considered unless status appears in the SET list, and
-- `old.status is distinct from new.status` means re-saving an already-resolved
-- alert notifies nobody a second time.
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

    -- distinct: someone who reported four sightings gets one thank-you.
    select array_agg(distinct s.reporter_id) into v_ids
    from public.sightings s
    where s.alert_id = new.id
      and s.reporter_id is not null
      and s.reporter_id <> new.user_id;

    perform public.notify_users(
        v_ids,
        new.id,
        'resolved',
        'resolved',
        jsonb_build_object('pet_name', v_pet_name)
    );

    return null;
exception
    when others then
        raise warning 'epona: resolved fan-out failed for alert % (%)', new.id, sqlerrm;
        return null;
end;
$$;

create trigger trg_alerts_notify_resolved
after update of status on public.alerts
for each row
when (old.status is distinct from new.status and new.status::text = 'resolved')
execute function public.tg_alert_notify_resolved();


-- 6d. resolved_at, while we are here
--
-- Adjacent bug, one line to fix, and it costs nothing to fix it in the same pass:
-- AlertService.resolveAlert sends {"status":"resolved"} and nothing else, so
-- alerts.resolved_at stays null forever and get_alert_detail hands the client a
-- resolved alert with no resolution date. BEFORE, so the stamped row is what gets
-- written and what the AFTER trigger above reads.
create or replace function public.tg_alert_stamp_resolved_at()
returns trigger
language plpgsql
security invoker
set search_path = public
as $$
begin
    new.resolved_at := coalesce(new.resolved_at, now());
    new.updated_at  := now();
    return new;
end;
$$;

create trigger trg_alerts_stamp_resolved_at
before update of status on public.alerts
for each row
when (old.status is distinct from new.status and new.status::text = 'resolved')
execute function public.tg_alert_stamp_resolved_at();


-- ---------------------------------------------------------------------------
-- 7. RLS on notifications
--
-- An inbox is the most personal table in this schema: it names the pets you own,
-- the alerts you responded to and roughly where you were when you did.
--
-- SELECT/UPDATE/DELETE own rows only. UPDATE is what
-- NotificationService.markAsRead needs, DELETE is deleteNotification, and both
-- carry the same predicate in USING and WITH CHECK so a row cannot be updated into
-- someone else's inbox.
--
-- There is no INSERT policy, and INSERT is revoked at the table level as well.
-- Privileges are checked before policies, so the revoke is the real lock and the
-- missing policy is the second one. The triggers above are unaffected: they are
-- SECURITY DEFINER, so they run as the table owner, and an owner is not subject to
-- its own table's policies unless the table is FORCE ROW LEVEL SECURITY. (It must
-- not be. See supabase/preflight_notifications_check.sql query 4.)
--
-- mark_notifications_read is SECURITY INVOKER and keeps working: it updates
-- `where user_id = p_user_id and user_id = auth.uid()`, which is a subset of the
-- update policy's predicate.
--
-- Dropped by name first; there is no "create or replace policy".
-- ---------------------------------------------------------------------------
alter table public.notifications enable row level security;

drop policy if exists "notifications_select_own" on public.notifications;
drop policy if exists "notifications_update_own" on public.notifications;
drop policy if exists "notifications_delete_own" on public.notifications;

create policy "notifications_select_own"
on public.notifications for select
to authenticated
using (user_id = auth.uid());

create policy "notifications_update_own"
on public.notifications for update
to authenticated
using (user_id = auth.uid())
with check (user_id = auth.uid());

create policy "notifications_delete_own"
on public.notifications for delete
to authenticated
using (user_id = auth.uid());

revoke insert on public.notifications from anon, authenticated;
grant select, update, delete on public.notifications to authenticated;


-- ---------------------------------------------------------------------------
-- 8. Push
--
-- pg_net posts the batch to an Edge Function, which mints a Google OAuth2 token
-- from a service account and calls FCM HTTP v1. Legacy server keys are gone, and
-- Postgres cannot sign an RS256 assertion without pgcrypto gymnastics nobody
-- should maintain, so the token minting has to live in Deno.
--
-- FOR EACH STATEMENT with a transition table, not FOR EACH ROW. A new alert inserts
-- one row per neighbour in a single statement; a row-level trigger would mean 300
-- HTTP requests where this makes one carrying 300 recipients.
--
-- Recipients with no fcm_token are dropped here rather than in the Edge Function:
-- the row in their inbox already exists and is all they are owed.
--
-- pg_net queues into a table inside the calling transaction, so a rolled-back test
-- sends nothing. Verify with `select extversion from pg_extension where extname =
-- 'pg_net'` -- versions before 0.7 dispatched regardless of the outcome.
-- ---------------------------------------------------------------------------
create extension if not exists pg_net;

do $$
begin
    if to_regprocedure('net.http_post(text, jsonb, jsonb, jsonb, integer)') is null then
        raise warning 'epona: net.http_post not found -- enable the pg_net extension, '
                      'push delivery is inert until then';
    end if;
end
$$;


-- epona_secret
--
-- The Edge Function URL and the shared secret are not in this file and never will
-- be: this file is in git. They live in Supabase Vault, are read here as the
-- definer, and are unreachable from anon/authenticated (see the revoke below).
-- The exception handler covers a project where the vault extension is absent --
-- push degrades to "did not happen", it does not take the insert down with it.
create or replace function public.epona_secret(p_name text)
returns text
language plpgsql
stable
security definer
set search_path = public, vault, extensions
as $$
declare
    v text;
begin
    select s.decrypted_secret into v
    from vault.decrypted_secrets s
    where s.name = p_name;
    return v;
exception
    when others then
        return null;
end;
$$;


-- clear_fcm_tokens
--
-- FCM answers 404 UNREGISTERED for a token belonging to an uninstalled app. Left
-- alone those tokens are retried on every alert forever. The Edge Function sends
-- them back here.
--
-- service_role only, and deliberately so: this takes a token, not a user id, so
-- anyone who could call it and had harvested a token could silence that person's
-- device.
create or replace function public.clear_fcm_tokens(p_tokens text[])
returns integer
language plpgsql
volatile
security definer
set search_path = public
as $$
declare
    v_count integer := 0;
begin
    if p_tokens is null or cardinality(p_tokens) = 0 then
        return 0;
    end if;

    update public.users u
    set fcm_token  = null,
        updated_at = now()
    where u.fcm_token = any(p_tokens);

    get diagnostics v_count = row_count;
    return v_count;
end;
$$;


create or replace function public.tg_notifications_push()
returns trigger
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    v_url      text;
    v_key      text;
    v_messages jsonb;
begin
    -- Every FCM data value must be a string; alert_id is nullable, so it becomes
    -- '' rather than being dropped. The keys are fixed by
    -- EponaFirebaseMessagingService.onMessageReceived: title, body, alert_id, type.
    select jsonb_agg(jsonb_build_object(
               'notification_id', i.id::text,
               'token',           u.fcm_token,
               'title',           i.title,
               'body',            i.body,
               'type',            i.type::text,
               'alert_id',        coalesce(i.alert_id::text, '')
           ))
      into v_messages
      from inserted i
          join public.users u on u.id = i.user_id
     where u.fcm_token is not null
       and length(trim(u.fcm_token)) > 0;

    if v_messages is null then
        return null;   -- nobody in this batch has a registered device
    end if;

    v_url := public.epona_secret('epona_push_endpoint');
    v_key := public.epona_secret('epona_push_key');

    if v_url is null or v_key is null then
        raise warning 'epona: push skipped, vault secrets epona_push_endpoint / '
                      'epona_push_key are not set';
        return null;
    end if;

    perform net.http_post(
        url     := v_url,
        headers := jsonb_build_object(
            'Content-Type',     'application/json',
            'x-epona-push-key', v_key
        ),
        body    := jsonb_build_object('messages', v_messages),
        timeout_milliseconds := 5000
    );

    return null;
exception
    when others then
        raise warning 'epona: push dispatch failed (%)', sqlerrm;
        return null;
end;
$$;

create trigger trg_notifications_push
after insert on public.notifications
referencing new table as inserted
for each statement
execute function public.tg_notifications_push();


-- ---------------------------------------------------------------------------
-- 9. Realtime
--
-- NotificationService.observeNotifications subscribes to postgres_changes on
-- public.notifications, which does nothing at all unless the table is a member of
-- the supabase_realtime publication. A bare `alter publication ... add table`
-- errors when it already is, so this is conditional rather than idempotent by
-- luck.
--
-- REPLICA IDENTITY FULL because the client subscribes to every action, not just
-- INSERT. An INSERT always carries the full new row, but UPDATE and DELETE carry
-- only the replica identity -- with the default (primary key) Realtime has no
-- user_id on the old row, cannot evaluate the RLS policy above against it, and
-- drops the event. Marking unread-as-read would go unnoticed by every other
-- device. The table is small and short-lived; the extra WAL is not a concern here.
-- ---------------------------------------------------------------------------
alter table public.notifications replica identity full;

do $$
begin
    if not exists (select 1 from pg_publication where pubname = 'supabase_realtime') then
        raise warning 'epona: publication supabase_realtime does not exist -- '
                      'enable Realtime for this project';
    elsif exists (
        select 1 from pg_publication
        where pubname = 'supabase_realtime' and puballtables
    ) then
        raise notice 'epona: supabase_realtime publishes all tables, nothing to add';
    elsif not exists (
        select 1 from pg_publication_tables
        where pubname = 'supabase_realtime'
          and schemaname = 'public'
          and tablename  = 'notifications'
    ) then
        execute 'alter publication supabase_realtime add table public.notifications';
        raise notice 'epona: added public.notifications to supabase_realtime';
    end if;
end
$$;


-- ---------------------------------------------------------------------------
-- 10. Indexes
--
-- The first two serve queries that already exist in NotificationService:
-- getNotifications orders by created_at desc within a user, getUnreadCount counts
-- the unread ones. The third serves the resolved fan-out. The fourth is the one
-- this file adds load to -- every new alert scans users by location -- and is
-- created only if no GiST index on users exists, because the remote may well
-- already have one under a name this file cannot predict.
-- ---------------------------------------------------------------------------
create index if not exists notifications_user_created_idx
    on public.notifications (user_id, created_at desc);

create index if not exists notifications_user_unread_idx
    on public.notifications (user_id)
    where is_read = false;

create index if not exists sightings_alert_reporter_idx
    on public.sightings (alert_id, reporter_id);

do $$
begin
    if not exists (
        select 1
        from pg_index i
            join pg_class c  on c.oid = i.indexrelid
            join pg_am    am on am.oid = c.relam
        where i.indrelid = 'public.users'::regclass
          and am.amname  = 'gist'
    ) then
        execute 'create index users_location_gix on public.users using gist (location)';
        raise notice 'epona: created users_location_gix';
    end if;
end
$$;


-- ---------------------------------------------------------------------------
-- 11. Privileges
--
-- Supabase's default privileges grant EXECUTE on new public functions to anon and
-- authenticated, which would publish every one of these as a PostgREST RPC. For
-- notify_user that is an open endpoint for writing into any user's inbox, and for
-- epona_secret it is the vault. These revokes are the most load-bearing lines in
-- the file.
-- ---------------------------------------------------------------------------
revoke all on function public.notification_copy(text, text, jsonb)
    from public, anon, authenticated;
revoke all on function public.notifications_type_name()
    from public, anon, authenticated;
revoke all on function public.notify_user(uuid, uuid, text, text, text)
    from public, anon, authenticated;
revoke all on function public.notify_users(uuid[], uuid, text, text, jsonb)
    from public, anon, authenticated;
revoke all on function public.epona_secret(text)
    from public, anon, authenticated;
revoke all on function public.clear_fcm_tokens(text[])
    from public, anon, authenticated;
revoke all on function public.tg_sighting_notify_owner()   from public, anon, authenticated;
revoke all on function public.tg_alert_notify_nearby()     from public, anon, authenticated;
revoke all on function public.tg_alert_notify_resolved()   from public, anon, authenticated;
revoke all on function public.tg_alert_stamp_resolved_at() from public, anon, authenticated;
revoke all on function public.tg_notifications_push()      from public, anon, authenticated;

-- The Edge Function calls this one with the service role key.
grant execute on function public.clear_fcm_tokens(text[]) to service_role;

-- The manual/system escape hatch for the 'message' type, which has no trigger.
grant execute on function public.notify_user(uuid, uuid, text, text, text) to service_role;
grant execute on function public.notify_users(uuid[], uuid, text, text, jsonb) to service_role;

notify pgrst, 'reload schema';
