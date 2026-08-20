-- Phase 5: rule-based yoga results linked to kundali_snapshot.
-- Engine stamps calculation_engine_version=V1.3 when yogas are computed.

CREATE TABLE yoga_result (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    kundali_id                  BIGINT       NOT NULL REFERENCES kundali_snapshot(id) ON DELETE CASCADE,
    yoga_code                   VARCHAR(64)  NOT NULL,
    category_code               VARCHAR(32)  NOT NULL,
    display_name                VARCHAR(128) NOT NULL,
    present                     BOOLEAN      NOT NULL,
    strength_code               VARCHAR(32)  NULL,
    planet_codes_json           JSONB        NOT NULL DEFAULT '[]'::jsonb,
    houses_json                 JSONB        NOT NULL DEFAULT '[]'::jsonb,
    explanation                 TEXT         NOT NULL,
    rule_id                     VARCHAR(64)  NOT NULL,
    calculation_engine_version  VARCHAR(16)  NOT NULL,
    meta_json                   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_yoga_result_kundali_code UNIQUE (kundali_id, yoga_code)
);

CREATE INDEX idx_yoga_result_tenant ON yoga_result (tenant_id);
CREATE INDEX idx_yoga_result_kundali ON yoga_result (kundali_id);
CREATE INDEX idx_yoga_result_kundali_category ON yoga_result (kundali_id, category_code);
CREATE INDEX idx_yoga_result_present ON yoga_result (kundali_id, present);
