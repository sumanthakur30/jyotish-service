-- Phase 0–1 foundation: tenant workspace + birth profile source data.
-- Calculated planetary results are NOT stored here (Phase 2+ snapshots).

CREATE TABLE jyotish_workspace (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    name            VARCHAR(128) NOT NULL,
    timezone        VARCHAR(64)  NOT NULL DEFAULT 'Asia/Kolkata',
    locale          VARCHAR(16)  NOT NULL DEFAULT 'en',
    ayanamsa_code   VARCHAR(32)  NOT NULL DEFAULT 'LAHIRI',
    zodiac_system   VARCHAR(16)  NOT NULL DEFAULT 'SIDEREAL',
    chart_style     VARCHAR(32)  NOT NULL DEFAULT 'NORTH_INDIAN',
    settings_json   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ  NULL,
    CONSTRAINT uq_jyotish_workspace_tenant UNIQUE (tenant_id),
    CONSTRAINT ck_jyotish_workspace_zodiac CHECK (zodiac_system IN ('SIDEREAL', 'TROPICAL'))
);

CREATE TABLE birth_profile (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    display_name    VARCHAR(256) NOT NULL,
    gender          VARCHAR(32)  NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    notes           TEXT         NULL,
    client_ref      VARCHAR(64)  NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ  NULL,
    archived_at     TIMESTAMPTZ  NULL,
    CONSTRAINT ck_birth_profile_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_birth_profile_tenant ON birth_profile (tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_birth_profile_tenant_name ON birth_profile (tenant_id, lower(display_name))
    WHERE deleted_at IS NULL;

-- Original birth input — never overwrite with derived values.
CREATE TABLE birth_details (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    profile_id          BIGINT       NOT NULL REFERENCES birth_profile(id),
    birth_date          DATE         NOT NULL,
    birth_time          TIME         NULL,
    birth_time_unknown  BOOLEAN      NOT NULL DEFAULT FALSE,
    dst_observed        BOOLEAN      NOT NULL DEFAULT FALSE,
    time_zone           VARCHAR(64)  NOT NULL DEFAULT 'Asia/Kolkata',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_birth_details_profile UNIQUE (profile_id)
);

CREATE INDEX idx_birth_details_tenant ON birth_details (tenant_id);

CREATE TABLE birth_location (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    profile_id          BIGINT       NOT NULL REFERENCES birth_profile(id),
    place_name          VARCHAR(256) NOT NULL,
    country_code        VARCHAR(8)   NULL,
    latitude            NUMERIC(10, 7) NOT NULL,
    longitude           NUMERIC(10, 7) NOT NULL,
    time_zone           VARCHAR(64)  NOT NULL,
    coords_manual       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_birth_location_profile UNIQUE (profile_id)
);

CREATE INDEX idx_birth_location_tenant ON birth_location (tenant_id);
