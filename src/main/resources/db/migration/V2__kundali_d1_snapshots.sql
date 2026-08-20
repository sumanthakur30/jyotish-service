-- Phase 2: versioned Kundali D1 snapshots (never silently rewrite historical reports).

CREATE TABLE kundali_snapshot (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               VARCHAR(64)  NOT NULL,
    birth_profile_id        BIGINT       NULL REFERENCES birth_profile(id),
    display_name            VARCHAR(256) NOT NULL,
    birth_date              DATE         NOT NULL,
    birth_time              TIME         NULL,
    birth_time_unknown      BOOLEAN      NOT NULL DEFAULT FALSE,
    time_zone               VARCHAR(64)  NOT NULL,
    place_name              VARCHAR(256) NOT NULL,
    latitude                NUMERIC(10, 7) NOT NULL,
    longitude               NUMERIC(10, 7) NOT NULL,
    ayanamsa_code           VARCHAR(32)  NOT NULL DEFAULT 'LAHIRI',
    ayanamsa_deg            NUMERIC(12, 6) NOT NULL,
    zodiac_system           VARCHAR(16)  NOT NULL DEFAULT 'SIDEREAL',
    house_system            VARCHAR(32)  NOT NULL DEFAULT 'WHOLE_SIGN',
    chart_style             VARCHAR(32)  NOT NULL DEFAULT 'NORTH_INDIAN',
    calculation_engine_version VARCHAR(16) NOT NULL,
    julian_day_ut           NUMERIC(18, 8) NOT NULL,
    ascendant_longitude     NUMERIC(12, 6) NOT NULL,
    ascendant_sign_index    SMALLINT     NOT NULL,
    notes                   TEXT         NULL,
    input_json              JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_kundali_snapshot_zodiac CHECK (zodiac_system IN ('SIDEREAL', 'TROPICAL'))
);

CREATE INDEX idx_kundali_snapshot_tenant ON kundali_snapshot (tenant_id);
CREATE INDEX idx_kundali_snapshot_tenant_profile ON kundali_snapshot (tenant_id, birth_profile_id);
CREATE INDEX idx_kundali_snapshot_tenant_created ON kundali_snapshot (tenant_id, created_at DESC);

CREATE TABLE planetary_position (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               VARCHAR(64)  NOT NULL,
    kundali_id              BIGINT       NOT NULL REFERENCES kundali_snapshot(id) ON DELETE CASCADE,
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
    CONSTRAINT uq_planetary_position_kundali_planet UNIQUE (kundali_id, planet_code),
    CONSTRAINT ck_planetary_house CHECK (house BETWEEN 1 AND 12),
    CONSTRAINT ck_planetary_pada CHECK (pada BETWEEN 1 AND 4)
);

CREATE INDEX idx_planetary_position_tenant ON planetary_position (tenant_id);
CREATE INDEX idx_planetary_position_kundali ON planetary_position (kundali_id);

CREATE TABLE house_position (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               VARCHAR(64)  NOT NULL,
    kundali_id              BIGINT       NOT NULL REFERENCES kundali_snapshot(id) ON DELETE CASCADE,
    house                   SMALLINT     NOT NULL,
    sign_index              SMALLINT     NOT NULL,
    sign_name               VARCHAR(32)  NOT NULL,
    cusp_longitude_deg      NUMERIC(12, 6) NOT NULL,
    CONSTRAINT uq_house_position_kundali_house UNIQUE (kundali_id, house),
    CONSTRAINT ck_house_number CHECK (house BETWEEN 1 AND 12)
);

CREATE INDEX idx_house_position_tenant ON house_position (tenant_id);
CREATE INDEX idx_house_position_kundali ON house_position (kundali_id);
