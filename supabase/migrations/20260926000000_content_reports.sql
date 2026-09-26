-- Content reporting: lets a user flag someone else's alert for moderation
-- review. Alerts only -- sightings are not reportable.
--
-- Google Play's User Generated Content policy requires apps whose users interact
-- through public posts to offer in-app reporting of content and users. Epona has
-- had none until now -- DetailHero's overflow menu calls straight through to
-- ReportContentUseCase after this file lands.
--
-- Reports are write-only from the client: RLS grants INSERT and nothing else, so
-- nobody can read who reported what, or how many times a given user has been
-- flagged, through the Data API. Moderation happens from the Supabase dashboard
-- (service_role bypasses RLS) or a future admin tool, not from the app.
--
-- No CREATE TABLE exists yet for `alerts` in this repo -- same situation as
-- 20260916120000_notification_triggers.sql documents -- so alerts.user_id is
-- taken on faith from AlertMapper and the RPCs already in this directory.

create table if not exists public.content_reports (
    id               uuid primary key default gen_random_uuid(),
    -- Defaults to the caller, and is nullable so account deletion can anonymise a
    -- report instead of deleting it outright -- see delete_user_data in
    -- 20260926000100_account_deletion.sql. ON DELETE SET NULL is a second,
    -- independent safety net if a user row is ever removed by some other path.
    reporter_id      uuid references auth.users(id) on delete set null default auth.uid(),
    -- No FK to public.alerts: its base DDL isn't in this repo (see above), and a
    -- CASCADE here would delete the very moderation record a removed alert most
    -- needs. tg_content_reports_fill below validates it exists at insert time
    -- instead, which is all a report needs.
    alert_id         uuid not null,
    -- Filled by tg_content_reports_fill below, never by the client -- see there
    -- for why it can't just be another column the client sends.
    reported_user_id uuid,
    reason           text not null,
    details          text,
    status           text not null default 'open',
    created_at       timestamptz not null default now()
);

do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conrelid = 'public.content_reports'::regclass
          and conname  = 'content_reports_reason_valid'
    ) then
        alter table public.content_reports
            add constraint content_reports_reason_valid
            check (reason in (
                'scam_or_fraud', 'false_information', 'inappropriate',
                'harassment', 'animal_cruelty', 'spam', 'other'
            ));
    end if;

    if not exists (
        select 1 from pg_constraint
        where conrelid = 'public.content_reports'::regclass
          and conname  = 'content_reports_status_valid'
    ) then
        alter table public.content_reports
            add constraint content_reports_status_valid
            check (status in ('open', 'reviewed', 'actioned', 'dismissed'));
    end if;

    if not exists (
        select 1 from pg_constraint
        where conrelid = 'public.content_reports'::regclass
          and conname  = 'content_reports_details_length'
    ) then
        alter table public.content_reports
            add constraint content_reports_details_length
            check (details is null or char_length(details) <= 500);
    end if;
end
$$;

comment on table public.content_reports is
    'Flags raised by users against an alert, for moderation. Write-only from '
    'the Data API -- see the RLS policy below.';
comment on column public.content_reports.reported_user_id is
    'Filled server-side from the target row: alerts.user_id.';

-- One report per (reporter, alert) pair. A repeat submission from the same
-- person on the same alert is a 23505 unique_violation that ReportRepositoryImpl
-- treats as success rather than surfacing an error -- reporting again isn't a
-- failure from the user's point of view.
create unique index if not exists content_reports_reporter_alert_key
    on public.content_reports (reporter_id, alert_id);


-- ---------------------------------------------------------------------------
-- tg_content_reports_fill
--
-- Two jobs in one BEFORE INSERT trigger: refuse a report against an alert_id
-- that doesn't exist (a stale screen, a deleted alert, a replayed request), and
-- fill reported_user_id from that alert's owner. Both have to happen
-- server-side -- the client cannot be trusted to send an honest
-- reported_user_id, and letting it try would mean an RLS policy or a second
-- round trip to look the real one up.
--
-- SECURITY DEFINER because the reporter has no SELECT grant on alerts via this
-- path in general, and doesn't need one just to file a report.
-- ---------------------------------------------------------------------------
create or replace function public.tg_content_reports_fill()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    select a.user_id into new.reported_user_id
    from public.alerts a
    where a.id = new.alert_id;

    if not found then
        raise exception 'Alert % does not exist', new.alert_id
            using errcode = '22023';
    end if;

    return new;
end;
$$;

drop trigger if exists trg_content_reports_fill on public.content_reports;

create trigger trg_content_reports_fill
before insert on public.content_reports
for each row
execute function public.tg_content_reports_fill();


-- ---------------------------------------------------------------------------
-- RLS
--
-- Insert-only, own reports only. There is deliberately no SELECT policy: a
-- reporter should not be able to see whether their report was actioned by
-- querying the table, and nobody should be able to enumerate who has been
-- reported. Moderators use the Supabase dashboard, which connects as postgres
-- and is unaffected by RLS.
-- ---------------------------------------------------------------------------
alter table public.content_reports enable row level security;

drop policy if exists "content_reports_insert_own" on public.content_reports;

create policy "content_reports_insert_own"
on public.content_reports for insert
to authenticated
with check (reporter_id = auth.uid());

-- Column-level grant: the client sends alert_id/reason/details only. id,
-- reporter_id, status and created_at all come from column DEFAULTs, which a
-- column-level INSERT grant does not need to include.
revoke all on public.content_reports from anon, authenticated;
grant insert (alert_id, reason, details) on public.content_reports to authenticated;

revoke all on function public.tg_content_reports_fill() from public, anon, authenticated;

notify pgrst, 'reload schema';
