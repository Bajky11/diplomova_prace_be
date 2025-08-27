CREATE OR REPLACE FUNCTION get_filtered_events_with_cursor(
    _cursor_event_id BIGINT,
    _cursor_time TIMESTAMP,
    _limit INTEGER,
    _title TEXT,
    _category_ids BIGINT[],
    _start_time TIMESTAMP,
    _end_time TIMESTAMP,
    _latitude DOUBLE PRECISION,
    _longitude DOUBLE PRECISION,
    _radius_km DOUBLE PRECISION
)
RETURNS SETOF events
AS
$$
BEGIN
RETURN QUERY
SELECT e.price,
       e.created_at,
       e.created_by_account_id,
       e.currency_id,
       e.end_time,
       e.happening_on_location_id,
       e.id,
       e.start_time,
       e.description,
       e.image_url,
       e.title
FROM events e
-- 🕐 Cursor-based stránkování: vracej události po určitém čase
WHERE e.start_time >= _cursor_time

  -- ❗ Nevracej znovu tu samou událost, která byla poslední
  AND (_cursor_event_id IS NULL OR e.id != _cursor_event_id)

  -- 🔍 Filtrace podle názvu (case-insensitive, částečná shoda)
  --AND (_title IS NULL OR LOWER(e.title) LIKE CONCAT('%', _title, '%'))
  AND (_title IS NULL OR e.title ILIKE CONCAT('%', _title, '%'))

  -- 🏷️ Filtrace podle kategorií přes spojovací tabulku
  AND (
    cardinality(_category_ids) = 0
        OR EXISTS (SELECT 1
                   FROM event_selected_categories esc
                   WHERE esc.event_id = e.id
                     AND esc.category_id = ANY (_category_ids))
    )

  -- 📆 Filtrace podle data (bez ohledu na čas)
  AND (
    _start_time IS NULL
        -- Pokud není zadaný end_time → vše od daného dne dál
        OR (_end_time IS NULL AND e.start_time::DATE >= _start_time::DATE)
        -- Pokud je zadaný i end_time → vše mezi daty (včetně)
        OR (_end_time IS NOT NULL AND e.start_time::DATE BETWEEN _start_time::DATE AND _end_time::DATE)
    )

  -- Filtrace podle zeměpisné polohy
  AND (
    -- pokud není zadána lokace nebo radius, nespouštěj filtraci podle polohy
    _latitude IS NULL OR _longitude IS NULL OR _radius_km IS NULL
        OR EXISTS (
        -- hledej lokaci události, která leží v daném okruhu
        SELECT 1
        FROM locations l
        WHERE l.id = e.happening_on_location_id
          -- použij PostGIS funkci pro výpočet vzdálenosti (v metrech)
          AND ST_DWithin(
                geography(ST_MakePoint(l.longitude, l.latitude)), -- lokace události
                geography(ST_MakePoint(_longitude, _latitude)), -- hledané místo
                _radius_km * 1000 -- poloměr v metrech
              ))
    )

-- 🔽 Výsledky řaď chronologicky
ORDER BY e.start_time ASC, id ASC
    LIMIT _limit;
END;
$$ LANGUAGE plpgsql;