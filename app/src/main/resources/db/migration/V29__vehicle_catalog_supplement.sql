-- VehiclesDB does not contain every model sold in every market.
-- Keep a small application supplement for relevant Russian-market names.
INSERT INTO work.w_vehicle_model(make_id, name)
SELECT id, model_name
FROM work.w_vehicle_make
CROSS JOIN (VALUES
  ('Haval', 'H3'),
  ('Haval', 'H3 второго поколения')
) AS models(make_name, model_name)
WHERE work.w_vehicle_make.name = models.make_name
ON CONFLICT (make_id, name) DO NOTHING;
