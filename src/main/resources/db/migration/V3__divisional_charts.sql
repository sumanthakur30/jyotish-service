-- Phase 3: divisional (varga) charts linked to kundali_snapshot.
-- Engine stamps calculation_engine_version=V1.1 when Vargas are computed.
-- D9 is persisted eagerly on kundali generate; other implemented Vargas lazy-compute on first GET.

CREATE TABLE divisional_chart (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    kundali_id                  BIGINT       NOT NULL REFERENCES kundali_snapshot(id) ON DELETE CASCADE,
    varga_code                  VARCHAR(8)   NOT NULL,
    calculation_engine_version  VARCHAR(16)  NOT NULL,
    house_system                VARCHAR(32)  NOT NULL DEFAULT 'WHOLE_SIGN',
    ascendant_longitude         NUMERIC(12, 6) NOT NULL,
    ascendant_sign_index        SMALLINT     NOT NULL,
    notes                       TEXT         NULL,
    meta_json                   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_divisional_chart_kundali_varga UNIQUE (kundali_id, varga_code),
    CONSTRAINT ck_divisional_varga_code CHECK (varga_code ~ '^D[0-9]+$')
);

CREATE INDEX idx_divisional_chart_tenant ON divisional_chart (tenant_id);
CREATE INDEX idx_divisional_chart_kundali ON divisional_chart (kundali_id);

CREATE TABLE divisional_planet_position (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               VARCHAR(64)  NOT NULL,
    divisional_chart_id     BIGINT       NOT NULL REFERENCES divisional_chart(id) ON DELETE CASCADE,
    planet_code             VARCHAR(32)  NOT NULL,
    longitude_deg           NUMERIC(12, 6) NOT NULL,
    sign_index              SMALLINT     NOT NULL,
    sign_name               VARCHAR(32)  NOT NULL,
    degree_in_sign          NUMERIC(12, 6) NOT NULL,
    house                   SMALLINT     NOT NULL,
    nakshatra_index         SMALLINT     NOT NULL,
    nakshatra_name          VARCHAR(64)  NOT NULL,
    pada                    SMALLINT     NOT NULL,
    retrograde              BOOLEAN      NOT NULL DEFAULT FALSE,
    combust                 BOOLEAN      NOT NULL DEFAULT FALSE,
    speed_deg_per_day       NUMERIC(12, 6) NULL,
    CONSTRAINT uq_div_planet_chart_planet UNIQUE (divisional_chart_id, planet_code),
    CONSTRAINT ck_div_planet_house CHECK (house BETWEEN 1 AND 12),
    CONSTRAINT ck_div_planet_pada CHECK (pada BETWEEN 1 AND 4)
);

CREATE INDEX idx_div_planet_tenant ON divisional_planet_position (tenant_id);
CREATE INDEX idx_div_planet_chart ON divisional_planet_position (divisional_chart_id);

CREATE TABLE divisional_house_position (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               VARCHAR(64)  NOT NULL,
    divisional_chart_id     BIGINT       NOT NULL REFERENCES divisional_chart(id) ON DELETE CASCADE,
    house                   SMALLINT     NOT NULL,
    sign_index              SMALLINT     NOT NULL,
    sign_name               VARCHAR(32)  NOT NULL,
    cusp_longitude_deg      NUMERIC(12, 6) NOT NULL,
    CONSTRAINT uq_div_house_chart_house UNIQUE (divisional_chart_id, house),
    CONSTRAINT ck_div_house_number CHECK (house BETWEEN 1 AND 12)
);

CREATE INDEX idx_div_house_tenant ON divisional_house_position (tenant_id);
CREATE INDEX idx_div_house_chart ON divisional_house_position (divisional_chart_id);
