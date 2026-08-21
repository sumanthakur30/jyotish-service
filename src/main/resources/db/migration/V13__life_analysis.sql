-- Life Analysis: structured Jyotish case notes (separate from calculated chart data).

CREATE TABLE IF NOT EXISTS life_analysis (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    kundali_id      BIGINT       NOT NULL REFERENCES kundali_snapshot (id) ON DELETE CASCADE,
    category        VARCHAR(32)  NOT NULL,
    sub_category    VARCHAR(64),
    status          VARCHAR(24)  NOT NULL DEFAULT 'NOT_STARTED',
    past_notes      TEXT,
    present_notes   TEXT,
    future_notes    TEXT,
    important_periods_notes TEXT,
    advice          TEXT,
    jyotish_notes   TEXT,
    sections_json   TEXT,
    include_in_report BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_life_analysis_kundali_cat UNIQUE (tenant_id, kundali_id, category, sub_category)
);

CREATE INDEX IF NOT EXISTS idx_life_analysis_kundali
    ON life_analysis (tenant_id, kundali_id);

CREATE TABLE IF NOT EXISTS life_analysis_period (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    kundali_id      BIGINT       NOT NULL REFERENCES kundali_snapshot (id) ON DELETE CASCADE,
    category        VARCHAR(32)  NOT NULL,
    from_date       DATE,
    to_date         DATE,
    topic           VARCHAR(128) NOT NULL,
    observation     TEXT,
    calculation_basis VARCHAR(256),
    status          VARCHAR(24)  NOT NULL DEFAULT 'PLANNED',
    sort_order      INT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_life_analysis_period_kundali
    ON life_analysis_period (tenant_id, kundali_id, category);

CREATE TABLE IF NOT EXISTS life_analysis_history (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    life_analysis_id BIGINT      NOT NULL REFERENCES life_analysis (id) ON DELETE CASCADE,
    field_name      VARCHAR(64)  NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    updated_by      VARCHAR(128),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_life_analysis_history_parent
    ON life_analysis_history (tenant_id, life_analysis_id, created_at DESC);

CREATE TABLE IF NOT EXISTS life_analysis_consultation (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    kundali_id      BIGINT       NOT NULL REFERENCES kundali_snapshot (id) ON DELETE CASCADE,
    category        VARCHAR(32)  NOT NULL,
    observation     TEXT,
    dasha_snapshot  TEXT,
    gochar_snapshot TEXT,
    advice          TEXT,
    follow_up_date  DATE,
    created_by      VARCHAR(128),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_life_analysis_consult_kundali
    ON life_analysis_consultation (tenant_id, kundali_id, created_at DESC);
