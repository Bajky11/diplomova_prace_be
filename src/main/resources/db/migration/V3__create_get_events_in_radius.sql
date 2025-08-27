CREATE OR REPLACE FUNCTION get_events_in_radius(
    _latitude DOUBLE PRECISION,
    _longitude DOUBLE PRECISION,
    _radius_km DOUBLE PRECISION,
    _limit INTEGER
)
RETURNS SETOF events AS
$$
BEGIN
RETURN QUERY
SELECT e.*
FROM events e
         JOIN locations l ON l.id = e.happening_on_location_id
WHERE ST_DWithin(
        geography(ST_MakePoint(l.longitude, l.latitude)),
        geography(ST_MakePoint(_longitude, _latitude)),
        _radius_km * 1000
      )
  AND e.start_time::DATE >= CURRENT_DATE
ORDER BY e.start_time ASC, e.id ASC
    LIMIT _limit;
END;
$$ LANGUAGE plpgsql;