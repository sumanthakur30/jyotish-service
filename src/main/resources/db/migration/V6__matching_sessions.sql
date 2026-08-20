-- Phase 6: Kundali matching sessions (Ashta Koota + Manglik).
-- Engine stamps calculation_engine_version=V1.4 when matching is computed.

CREATE TABLE matching_session (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    profile_id_a                BIGINT       NOT NULL REFERENCES birth_profile(id),
    profile_id_b                BIGINT       NOT NULL REFERENCES birth_profile(id),
    kundali_id_a                BIGINT       NULL REFERENCES kundali_snapshot(id) ON DELETE SET NULL,
    kundali_id_b                BIGINT       NULL REFERENCES kundali_snapshot(id) ON DELETE SET NULL,
    display_name_a              VARCHAR(256) NOT NULL,
    display_name_b              VARCHAR(256) NOT NULL,
    total_score                 INT          NOT NULL,
    max_score                   INT          NOT NULL,
    percentage                  NUMERIC(6,2) NOT NULL,
    manglik_status_a            VARCHAR(16)  NOT NULL,
    manglik_status_b            VARCHAR(16)  NOT NULL,
    manglik_mars_house_a        SMALLINT     NOT NULL,
    manglik_mars_house_b        SMALLINT     NOT NULL,
    summary                     TEXT         NOT NULL,
    notes                       TEXT         NOT NULL,
    disclaimer                  TEXT         NOT NULL,
    calculation_engine_version  VARCHAR(16)  NOT NULL,
    result_json                 JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_matching_session_tenant ON matching_session (tenant_id);
CREATE INDEX idx_matching_session_profiles ON matching_session (tenant_id, profile_id_a, profile_id_b);
CREATE INDEX idx_matching_session_created ON matching_session (tenant_id, created_at DESC);

CREATE TABLE matching_koota_score (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    matching_id                 BIGINT       NOT NULL REFERENCES matching_session(id) ON DELETE CASCADE,
    koota_code                  VARCHAR(32)  NOT NULL,
    display_name                VARCHAR(64)  NOT NULL,
    obtained                    INT          NOT NULL,
    max_points                  INT          NOT NULL,
    explanation                 TEXT         NOT NULL,
    rule_id                     VARCHAR(64)  NOT NULL,
    sort_order                  SMALLINT     NOT NULL,
    CONSTRAINT uq_matching_koota UNIQUE (matching_id, koota_code)
);

CREATE INDEX idx_matching_koota_session ON matching_koota_score (matching_id);
CREATE INDEX idx_matching_koota_tenant ON matching_koota_score (tenant_id);
