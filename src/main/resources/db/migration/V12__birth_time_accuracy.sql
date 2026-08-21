ALTER TABLE birth_details
  ADD COLUMN IF NOT EXISTS birth_time_accuracy VARCHAR(16),
  ADD COLUMN IF NOT EXISTS uncertainty_minutes INTEGER;

COMMENT ON COLUMN birth_details.birth_time_accuracy IS 'EXACT | APPROXIMATE | UNKNOWN (null = derive from birth_time_unknown)';
COMMENT ON COLUMN birth_details.uncertainty_minutes IS 'For APPROXIMATE: 5, 15, 30, or 60';
