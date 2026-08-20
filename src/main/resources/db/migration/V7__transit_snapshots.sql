-- Phase 7: Gochar / transit snapshots linked to kundali_snapshot.
-- Engine stamps calculation_engine_version=V1.5 when transit is computed.
-- Unique (kundali_id, transit_date) for lightweight reproducibility of a day's Gochar.

CREATE TABLE transit_snapshot (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    kundali_id                  BIGINT       NOT NULL REFERENCES kundali_snapshot(id) ON DELETE CASCADE,
    transit_date                DATE         NOT NULL,
    transit_time                TIME         NOT NULL,
    time_zone                   VARCHAR(64)  NOT NULL,
    julian_day_ut               NUMERIC(16,8) NOT NULL,
    ayanamsa_code               VARCHAR(32)  NOT NULL,
    ayanamsa_deg                NUMERIC(12,6) NOT NULL,
    natal_lagna_sign_index      SMALLINT     NOT NULL,
    system_code                 VARCHAR(32)  NOT NULL DEFAULT 'GOCHAR',
    calculation_engine_version  VARCHAR(16)  NOT NULL,
    notes                       TEXT         NULL,
    meta_json                   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_transit_kundali_date UNIQUE (kundali_id, transit_date)
);

CREATE INDEX idx_transit_snapshot_tenant ON transit_snapshot (tenant_id);
CREATE INDEX idx_transit_snapshot_kundali ON transit_snapshot (kundali_id);
CREATE INDEX idx_transit_snapshot_date ON transit_snapshot (tenant_id, transit_date DESC);

CREATE TABLE transit_planet_position (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    transit_id                  BIGINT       NOT NULL REFERENCES transit_snapshot(id) ON DELETE CASCADE,
    planet_code                 VARCHAR(32)  NOT NULL,
    longitude_deg               NUMERIC(12,6) NOT NULL,
    sign_index                  SMALLINT     NOT NULL,
    sign_name                   VARCHAR(32)  NOT NULL,
    degree_in_sign              NUMERIC(12,6) NOT NULL,
    house                       SMALLINT     NOT NULL,
    nakshatra_index             SMALLINT     NOT NULL,
    nakshatra_name              VARCHAR(64)  NOT NULL,
    pada                        SMALLINT     NOT NULL,
    retrograde                  BOOLEAN      NOT NULL DEFAULT FALSE,
    speed_deg_per_day           NUMERIC(12,6) NULL,
    natal_longitude_deg         NUMERIC(12,6) NULL,
    natal_sign_index            SMALLINT     NULL,
    natal_sign_name             VARCHAR(32)  NULL,
    natal_house                 SMALLINT     NULL,
    sign_changed                BOOLEAN      NOT NULL DEFAULT FALSE,
    house_changed               BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_transit_planet UNIQUE (transit_id, planet_code)
);

CREATE INDEX idx_transit_planet_transit ON transit_planet_position (transit_id);
CREATE INDEX idx_transit_planet_tenant ON transit_planet_position (tenant_id);
