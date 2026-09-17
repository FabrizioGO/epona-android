-- Computed columns exposing an alert's coordinates as plain numbers.
--
-- AlertService.getMyAlerts is an ordinary table select, not an RPC, and a geography
-- column comes back over Postgrest as WKB hex (0101000020E6100000...) rather than
-- anything the client can read. So AlertDto had no coordinates, AlertMapper cached
-- every row at (0, 0), and both the My Alerts screen and the offline nearby-alerts
-- fallback -- which reads those same cached rows -- placed alerts off the coast of
-- Africa.
--
-- A function taking a single argument of the table's composite type is exposed by
-- Postgrest as a virtual column on that table. It is not part of `*`, so the client
-- has to name it: select("*,last_seen_lat,last_seen_lng").
--
-- Keep these names distinct from real columns on `alerts`, or the real column wins
-- and the function is never called.

create or replace function public.last_seen_lat(a public.alerts)
returns double precision
language sql
stable
security invoker
set search_path = public, extensions
as $$
    select st_y(a.last_seen_location::geometry);
$$;

create or replace function public.last_seen_lng(a public.alerts)
returns double precision
language sql
stable
security invoker
set search_path = public, extensions
as $$
    select st_x(a.last_seen_location::geometry);
$$;

grant execute on function public.last_seen_lat(public.alerts) to authenticated;
grant execute on function public.last_seen_lng(public.alerts) to authenticated;

notify pgrst, 'reload schema';
