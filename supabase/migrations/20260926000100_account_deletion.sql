-- Account deletion: one function that removes everything a user's account owns,
-- called by the delete-account Edge Function (supabase/functions/delete-account).
--
-- Google Play requires an in-app path to delete an account and its data, plus a
-- reachable web link for the same request -- see docs/delete-account.md. Neither
-- existed before this file: ProfileViewModel had no delete option, and there was
-- no server-side function that could safely remove a user's rows in the right
-- order.
--
-- "Right order" is the whole reason this is a function and not five DELETEs run
-- from the Edge Function: children have to go before parents, and the children
-- span three tables (notifications, sightings, alerts) before the row in `pets`
-- and finally `users` itself. Running that as separate PostgREST calls would also
-- mean five separate RLS evaluations with no transaction tying them together --
-- if the third one failed, the first two would already be gone.
--
-- SECURITY DEFINER, service_role only. This is not something a signed-in user
-- calls for themselves through PostgREST: the Edge Function authenticates the
-- caller's bearer token against GET /auth/v1/user itself (see index.ts), then
-- invokes this with the service role key. That split -- auth check in Deno,
-- unrestricted deletes in Postgres -- exists because a SECURITY DEFINER function
-- reachable by `authenticated` with only `p_user_id = auth.uid()` guarding it is
-- one dropped WHERE clause away from letting any signed-in user delete anyone.
-- Keeping it service_role-only removes that failure mode entirely.
--
-- No CREATE TABLE for any of this in the repo -- see the note in
-- 20260916120000_notification_triggers.sql -- so every column read below is taken
-- on faith from the DTOs/mappers and the RPCs already in this directory:
-- pets(id, owner_id, photo_urls), alerts(id, user_id, pet_id), sightings(id,
-- alert_id, reporter_id, photo_urls), users(id, avatar_url).
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
