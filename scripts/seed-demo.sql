-- Idempotent public demo data for Vi-vu-Viet.
-- Run from the project root with: psql -h localhost -p 5433 -U <user> -d project -v ON_ERROR_STOP=1 -f scripts/seed-demo.sql
BEGIN;

INSERT INTO bus_companies
    (id, created_at, updated_at, name, slug, hotline, timezone, status)
VALUES
    ('f0000000-0000-4000-8000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'Vi Vu Viet Seed Demo', 'vi-vu-viet-seed-demo', '1900 1234', 'Asia/Ho_Chi_Minh', 'ACTIVE')
ON CONFLICT DO NOTHING;

INSERT INTO areas
    (id, created_at, updated_at, code, name, type, status)
VALUES
    ('f0000000-0000-4000-8000-000000000010', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'HN-SEED', 'Hà Nội', 'PROVINCE', 'ACTIVE'),
    ('f0000000-0000-4000-8000-000000000011', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'DN-SEED', 'Đà Nẵng', 'PROVINCE', 'ACTIVE')
ON CONFLICT DO NOTHING;

INSERT INTO routes
    (id, created_at, updated_at, company_id, code, name, origin_area_id, destination_area_id,
     distance_km, duration_minutes, base_fare_amount, status)
VALUES
    ('f0000000-0000-4000-8000-000000000020', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'f0000000-0000-4000-8000-000000000001', 'HN-DN-SEED', 'Hà Nội – Đà Nẵng',
     'f0000000-0000-4000-8000-000000000010', 'f0000000-0000-4000-8000-000000000011',
     765.0, 900, 850000, 'ACTIVE'),
    ('f0000000-0000-4000-8000-000000000021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'f0000000-0000-4000-8000-000000000001', 'DN-HN-SEED', 'Đà Nẵng – Hà Nội',
     'f0000000-0000-4000-8000-000000000011', 'f0000000-0000-4000-8000-000000000010',
     765.0, 900, 850000, 'ACTIVE')
ON CONFLICT DO NOTHING;

INSERT INTO vehicles
    (id, created_at, updated_at, company_id, plate_normalized, vehicle_model, passenger_capacity, status)
VALUES
    ('f0000000-0000-4000-8000-000000000040', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'f0000000-0000-4000-8000-000000000001', '30F-SEED-001', 'Limousine 34 chỗ', 34, 'ACTIVE')
ON CONFLICT DO NOTHING;

INSERT INTO trips
    (id, created_at, updated_at, company_id, route_id, trip_code, departure_at,
     expected_arrival_at, booking_cutoff_at, status, sales_status)
VALUES
    ('f0000000-0000-4000-8000-000000000030', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'f0000000-0000-4000-8000-000000000001', 'f0000000-0000-4000-8000-000000000020',
     'SEED-HN-DN-0700', CURRENT_DATE + INTERVAL '2 days' + TIME '07:00',
     CURRENT_DATE + INTERVAL '2 days' + TIME '22:00', CURRENT_DATE + INTERVAL '2 days' + TIME '06:00',
     'SCHEDULED', 'OPEN'),
    ('f0000000-0000-4000-8000-000000000031', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'f0000000-0000-4000-8000-000000000001', 'f0000000-0000-4000-8000-000000000020',
     'SEED-HN-DN-1900', CURRENT_DATE + INTERVAL '2 days' + TIME '19:00',
     CURRENT_DATE + INTERVAL '3 days' + TIME '10:00', CURRENT_DATE + INTERVAL '2 days' + TIME '18:00',
     'SCHEDULED', 'OPEN'),
    ('f0000000-0000-4000-8000-000000000032', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
     'f0000000-0000-4000-8000-000000000001', 'f0000000-0000-4000-8000-000000000021',
     'SEED-DN-HN-2000', CURRENT_DATE + INTERVAL '3 days' + TIME '20:00',
     CURRENT_DATE + INTERVAL '4 days' + TIME '11:00', CURRENT_DATE + INTERVAL '3 days' + TIME '19:00',
     'SCHEDULED', 'OPEN')
ON CONFLICT DO NOTHING;

INSERT INTO trip_seats
    (id, created_at, updated_at, company_id, trip_id, seat_code, seat_class)
SELECT md5(trips.trip_id::text || ':' || seat_code)::uuid,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       'f0000000-0000-4000-8000-000000000001', trips.trip_id, seat_code, 'STANDARD'
FROM (VALUES
    ('f0000000-0000-4000-8000-000000000030'::uuid),
    ('f0000000-0000-4000-8000-000000000031'::uuid),
    ('f0000000-0000-4000-8000-000000000032'::uuid)
) AS trips(trip_id)
CROSS JOIN generate_series(1, 4) AS row_no
CROSS JOIN (VALUES ('A'), ('B')) AS side(letter)
CROSS JOIN LATERAL (SELECT side.letter || row_no::text AS seat_code) seats
ON CONFLICT DO NOTHING;

COMMIT;
