create or replace function get_events_in_bounds(
    west double precision,
    south double precision,
    east double precision,
    north double precision
)
returns table (
    id            bigint,
    latitude      double precision,
    longitude     double precision,
    categoryName text
)
language sql
as $$
select distinct on (e.id)
    e.id,
    l.latitude,
    l.longitude,
    c.name as category_name
from events e
    join locations l on e.happening_on_location_id = l.id
    left join event_selected_categories esc on e.id = esc.event_id
    left join categories c on esc.category_id = c.id
where ST_MakePoint(l.longitude, l.latitude)::geography &&
    ST_MakeEnvelope(west, south, east, north, 4326)::geography
  AND e.start_time::DATE >= CURRENT_DATE
order by e.id, c.name; -- nebo esc.category_id, pokud chceš podle pořadí v tabulce
$$;