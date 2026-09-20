-- Fix for create_found_alert RPC function
-- Adds enum casts for p_species and p_size parameters
--
-- Run this in Supabase SQL Editor if the migration hasn't been applied yet.
-- Reference: https://github.com/supabase/postgrest/issues/... (enum casting issue)

create or replace function public.create_found_alert(
    p_user_id          uuid,
    p_species          text,
    p_lat              double precision,
    p_lng              double precision,
    p_breed            text        default null,
    p_color            text        default null,
    p_size             text        default 'medium',
    p_pet_description  text        default null,
    p_photo_urls       text[]      default '{}',
    p_address          text        default null,
    p_last_seen_at     timestamptz default null,
    p_alert_description text       default null,
    p_contact_phone    text        default null
)
returns setof public.alerts
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    v_pet_id uuid;
begin
    if p_user_id is distinct from auth.uid() then
        raise exception 'A found alert can only be posted as the signed-in user'
            using errcode = '42501';
    end if;

    insert into public.pets (
        owner_id,
        name,
        species,
        breed,
        color,
        size,
        description,
        photo_urls
    )
    values (
        null,
        null,
        p_species::pet_species,
        nullif(p_breed, ''),
        nullif(p_color, ''),
        p_size::pet_size,
        p_pet_description,
        coalesce(p_photo_urls, '{}')
    )
    returning id into v_pet_id;

    return query
    insert into public.alerts (
        pet_id,
        user_id,
        type,
        status,
        last_seen_location,
        last_seen_address,
        last_seen_at,
        description,
        contact_phone
    )
    values (
        v_pet_id,
        p_user_id,
        'found',
        'active',
        st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography,
        p_address,
        coalesce(p_last_seen_at, now()),
        p_alert_description,
        p_contact_phone
    )
    returning *;
end;
$$;

grant execute on function public.create_found_alert(
    uuid, text, double precision, double precision,
    text, text, text, text, text[],
    text, timestamptz, text, text
) to authenticated;

notify pgrst, 'reload schema';