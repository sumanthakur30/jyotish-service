-- Allow Bhava Chalit as a non-varga chart row alongside D1–D60.
ALTER TABLE divisional_chart ALTER COLUMN varga_code TYPE VARCHAR(16);
ALTER TABLE divisional_chart DROP CONSTRAINT IF EXISTS ck_divisional_varga_code;
ALTER TABLE divisional_chart
  ADD CONSTRAINT ck_divisional_varga_code CHECK (varga_code ~ '^(D[0-9]+|CHALIT)$');
