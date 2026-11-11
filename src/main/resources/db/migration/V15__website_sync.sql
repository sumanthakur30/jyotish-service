-- JhaAstro (and later Astro sites) server-to-server upsert: idempotency + external ids.
-- Does not replace CRM tables; maps website events onto jyotish_client / birth_profile / appointment.

CREATE TABLE jyotish_website_sync_idempotency (
    id                BIGSERIAL PRIMARY KEY,
    tenant_id         VARCHAR(64)   NOT NULL,
    idempotency_key   VARCHAR(128)  NOT NULL,
    response_json     TEXT          NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_jyotish_website_sync_idem UNIQUE (tenant_id, idempotency_key)
);

CREATE TABLE jyotish_website_external_id (
    id                BIGSERIAL PRIMARY KEY,
    tenant_id         VARCHAR(64)   NOT NULL,
    entity_type       VARCHAR(32)   NOT NULL,
    external_id       VARCHAR(128)  NOT NULL,
    internal_id       BIGINT        NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_jyotish_website_ext UNIQUE (tenant_id, entity_type, external_id)
);

CREATE INDEX idx_jyotish_website_ext_internal
    ON jyotish_website_external_id (tenant_id, entity_type, internal_id);
