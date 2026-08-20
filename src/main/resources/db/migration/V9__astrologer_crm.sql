-- Phase 9: Astrologer CRM — clients, birth-profile links, appointments.
-- Independent of hospital appointment-service.

CREATE TABLE jyotish_client (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    name            VARCHAR(256) NOT NULL,
    mobile          VARCHAR(32)  NULL,
    email           VARCHAR(256) NULL,
    notes           TEXT         NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ  NULL
);

CREATE INDEX idx_jyotish_client_tenant ON jyotish_client (tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_jyotish_client_tenant_name ON jyotish_client (tenant_id, lower(name))
    WHERE deleted_at IS NULL;
CREATE INDEX idx_jyotish_client_tenant_mobile ON jyotish_client (tenant_id, mobile)
    WHERE deleted_at IS NULL AND mobile IS NOT NULL;

CREATE TABLE jyotish_client_birth_profile (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    client_id           BIGINT       NOT NULL REFERENCES jyotish_client(id) ON DELETE CASCADE,
    birth_profile_id    BIGINT       NOT NULL REFERENCES birth_profile(id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_jyotish_client_birth_profile UNIQUE (client_id, birth_profile_id)
);

CREATE INDEX idx_jyotish_client_bp_tenant ON jyotish_client_birth_profile (tenant_id);
CREATE INDEX idx_jyotish_client_bp_client ON jyotish_client_birth_profile (client_id);
CREATE INDEX idx_jyotish_client_bp_profile ON jyotish_client_birth_profile (birth_profile_id);

CREATE TABLE jyotish_appointment (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    client_id           BIGINT       NOT NULL REFERENCES jyotish_client(id),
    appointment_date    DATE         NOT NULL,
    appointment_time    TIME         NOT NULL,
    consultation_type   VARCHAR(64)  NOT NULL,
    status              VARCHAR(32)  NOT NULL DEFAULT 'SCHEDULED',
    payment_status      VARCHAR(32)  NULL,
    notes               TEXT         NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ  NULL,
    CONSTRAINT ck_jyotish_appointment_status CHECK (
        status IN ('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')
    ),
    CONSTRAINT ck_jyotish_appointment_payment CHECK (
        payment_status IS NULL
        OR payment_status IN ('UNPAID', 'PENDING', 'PAID', 'WAIVED')
    )
);

CREATE INDEX idx_jyotish_appointment_tenant ON jyotish_appointment (tenant_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_jyotish_appointment_tenant_date ON jyotish_appointment (tenant_id, appointment_date)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_jyotish_appointment_client ON jyotish_appointment (client_id)
    WHERE deleted_at IS NULL;
