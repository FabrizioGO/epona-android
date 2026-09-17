-- public.create_alert
--
-- Inserts an alert, building the PostGIS point from the lat/lng the client sends.
-- Postgrest cannot insert a geography column directly, which is why this is an RPC
-- rather than a plain table insert (see AlertInsertParams in core/network/dto).
--
-- Called from AlertService.createAlert as POST /rest/v1/rpc/create_alert.
--
-- Two things the Kotlin client constrains, both easy to get wrong:
--
--   1. Every optional parameter needs a DEFAULT. The 404 that prompted this file
--      named nine arguments, not ten: p_last_seen_at took no part in the lookup,
--      because AlertMapper.toInsertParams hardcodes it to null so the server stamps
--      the time. Any optional field left blank drops out the same way, and Postgrest
--      resolves the function by whichever set of names arrived.
--
--   2. It has to return a SET, not a single composite. PostgrestResult.decodeSingle()
--      is decodeList().first(), so the client is parsing a JSON array; a function
--      declared `returns public.alerts` hands back a bare object and fails to decode.

create or replace function public.create_alert(
    p_pet_id        uuid,
    p_user_id       uuid,
    p_type          text,
    p_lat           double precision,
    p_lng           double precision,
    p_address       text        default null,
    p_last_seen_at  timestamptz default null,
    p_description   text        default null,
    p_reward        numeric     default null,
    p_contact_phone text        default null
)
returns setof public.alerts
language sql
security invoker
set search_path = public, extensions
as $$
    insert into public.alerts (
        pet_id,
        user_id,
        type,
        status,
        last_seen_location,
        last_seen_address,
        last_seen_at,
        description,
        reward,
        contact_phone
    )
    values (
        p_pet_id,
        p_user_id,
        -- alerts.type is the enum alert_type, and a text variable needs the cast
        -- spelled out. The bare literal on the next line coerces on its own.
        p_type::alert_type,
        'active',
        -- ST_MakePoint is (x, y) — longitude first, then latitude.
        st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography,
        p_address,
        coalesce(p_last_seen_at, now()),
        p_description,
        p_reward,
        p_contact_phone
    )
    returning *;
$$;

grant execute on function public.create_alert(
    uuid, uuid, text, double precision, double precision,
    text, timestamptz, text, numeric, text
) to authenticated;

-- Postgrest caches the schema. Supabase reloads it on DDL, this makes it immediate.
notify pgrst, 'reload schema';
