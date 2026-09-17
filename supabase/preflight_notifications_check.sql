-- Pre-flight checks for supabase/migrations/<ts>_notification_triggers.sql
--
-- Run every query below in the Supabase SQL editor BEFORE applying the migration.
-- There is no CREATE TABLE anywhere in this repo, so this is the only way to confirm
-- the migration's assumptions about a schema that lives only in the remote project.
-- Delete this file once you've verified everything (it is not part of the migration).

-- 1. Exact shape of notifications. Watch for a NOT NULL column with no default that
--    the triggers don't populate, and whether `type` is text or an enum.
select column_name, data_type, udt_name, is_nullable, column_default
from information_schema.columns
where table_schema = 'public' and table_name = 'notifications'
order by ordinal_position;

-- 2. Constraints on notifications. A CHECK on `type` that doesn't list our four
--    values fails every trigger insert (silently -- the triggers catch and warn).
select conname, pg_get_constraintdef(oid)
from pg_constraint where conrelid = 'public.notifications'::regclass;

-- 3. If `type` is an enum, are the four labels present?
select t.typname, e.enumlabel
from pg_type t join pg_enum e on e.enumtypid = t.oid
where t.typname in ('notification_type', 'alert_type', 'alert_status')
order by t.typname, e.enumsortorder;

-- 4. Ownership and FORCE RLS. If notifications is NOT owned by `postgres`, or has
--    relforcerowsecurity = true, the SECURITY DEFINER triggers will be blocked by
--    the RLS policies the migration adds, and every insert fails.
select c.relname, pg_get_userbyid(c.relowner) as owner,
       c.relrowsecurity as rls_enabled, c.relforcerowsecurity as rls_forced,
       c.relreplident
from pg_class c
where c.oid in ('public.notifications'::regclass, 'public.users'::regclass);

-- 5. Existing policies on notifications. Permissive policies OR together -- a stray
--    `using (true)` keeps leaking after the migration ships.
select policyname, cmd, roles, qual, with_check
from pg_policies where schemaname = 'public' and tablename = 'notifications';

-- 6. The alert_id FK. If it has no ON DELETE action, AlertService.deleteAlert starts
--    failing with 23503 once notifications reference it -- a regression this feature
--    would introduce into a currently-working code path.
select conname, pg_get_constraintdef(oid)
from pg_constraint
where conrelid = 'public.notifications'::regclass and contype = 'f';

-- 7. Column-level grants on users. If present, the client's `locale` PATCH could be
--    rejected even though other UserUpdateDto fields work today.
select grantee, privilege_type, column_name
from information_schema.column_privileges
where table_schema = 'public' and table_name = 'users';

-- 8. pg_net version -- versions before 0.7 could dispatch a queued HTTP call even
--    from a transaction that later rolls back, which matters for the verification
--    steps that wrap trigger tests in BEGIN/ROLLBACK.
select extname, extversion from pg_extension where extname = 'pg_net';

-- 9. Any existing trigger on alerts/sightings/notifications, so the new ones don't
--    duplicate or conflict with something already there.
select c.relname as table_name, t.tgname, t.tgenabled
from pg_trigger t join pg_class c on c.oid = t.tgrelid
where not t.tgisinternal
  and c.oid in ('public.alerts'::regclass,
                'public.sightings'::regclass,
                'public.notifications'::regclass)
order by 1, 2;

-- 10. Existing GiST index on users(location), so the migration doesn't create a
--     duplicate under a different name.
select i.indexrelid::regclass as index_name, am.amname
from pg_index i
    join pg_class c  on c.oid = i.indrelid
    join pg_am    am on am.oid = (select relam from pg_class where oid = i.indexrelid)
where i.indrelid = 'public.users'::regclass;
