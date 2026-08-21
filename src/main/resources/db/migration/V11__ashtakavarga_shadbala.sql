-- V1.7: Ashtakavarga + partial Shadbala results (lazy compute+persist per kundali).

CREATE TABLE ashtakavarga_result (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    kundali_id                  BIGINT       NOT NULL REFERENCES kundali_snapshot(id) ON DELETE CASCADE,
    result_json                 JSONB        NOT NULL,
    calculation_engine_version  VARCHAR(16)  NOT NULL,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ashtakavarga_result_kundali UNIQUE (kundali_id)
);

CREATE INDEX idx_ashtakavarga_result_tenant ON ashtakavarga_result (tenant_id);
CREATE INDEX idx_ashtakavarga_result_kundali ON ashtakavarga_result (kundali_id);

CREATE TABLE shadbala_result (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    kundali_id                  BIGINT       NOT NULL REFERENCES kundali_snapshot(id) ON DELETE CASCADE,
    result_json                 JSONB        NOT NULL,
    calculation_engine_version  VARCHAR(16)  NOT NULL,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_shadbala_result_kundali UNIQUE (kundali_id)
);

CREATE INDEX idx_shadbala_result_tenant ON shadbala_result (tenant_id);
CREATE INDEX idx_shadbala_result_kundali ON shadbala_result (kundali_id);
