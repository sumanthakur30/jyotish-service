-- Phase 4: Vimshottari (and future) dasha periods linked to kundali_snapshot.
-- Engine stamps calculation_engine_version=V1.2 when dasha is computed.

CREATE TABLE dasha_period (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    kundali_id                  BIGINT       NOT NULL REFERENCES kundali_snapshot(id) ON DELETE CASCADE,
    system_code                 VARCHAR(32)  NOT NULL,
    level_code                  VARCHAR(16)  NOT NULL,
    lord_code                   VARCHAR(32)  NOT NULL,
    maha_lord_code              VARCHAR(32)  NOT NULL,
    antar_lord_code             VARCHAR(32)  NULL,
    pratyantar_lord_code        VARCHAR(32)  NULL,
    sequence_no                 INT          NOT NULL,
    start_at                    TIMESTAMPTZ  NOT NULL,
    end_at                      TIMESTAMPTZ  NOT NULL,
    calculation_engine_version  VARCHAR(16)  NOT NULL,
    meta_json                   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_dasha_level CHECK (level_code IN ('MAHA', 'ANTAR', 'PRATYANTAR')),
    CONSTRAINT ck_dasha_range CHECK (end_at > start_at)
);

CREATE INDEX idx_dasha_period_tenant ON dasha_period (tenant_id);
CREATE INDEX idx_dasha_period_kundali ON dasha_period (kundali_id);
CREATE INDEX idx_dasha_period_kundali_system ON dasha_period (kundali_id, system_code);
CREATE INDEX idx_dasha_period_lookup
    ON dasha_period (kundali_id, system_code, level_code, start_at);
