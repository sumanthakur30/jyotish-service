-- Phase 10: lightweight audit/meter for AI Jyotish Assistant asks.
-- Context is always built from verified engine/DB snapshots — never invented ephemeris.

CREATE TABLE jyotish_ai_ask (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    kundali_id          BIGINT       NOT NULL REFERENCES kundali_snapshot(id),
    topic               VARCHAR(64)  NULL,
    question            TEXT         NOT NULL,
    provider_code       VARCHAR(32)  NOT NULL,
    model_code          VARCHAR(64)  NULL,
    latency_ms          INTEGER      NULL,
    context_summary     TEXT         NULL,
    answer_preview      TEXT         NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_jyotish_ai_ask_tenant ON jyotish_ai_ask (tenant_id);
CREATE INDEX idx_jyotish_ai_ask_kundali ON jyotish_ai_ask (kundali_id);
CREATE INDEX idx_jyotish_ai_ask_tenant_created ON jyotish_ai_ask (tenant_id, created_at DESC);
