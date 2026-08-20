-- Phase 8: PDF report metadata (bytes live on filesystem under jyotish.reports.storage-dir).

CREATE TABLE kundali_report (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    report_type                 VARCHAR(32)  NOT NULL,
    kundali_id                  BIGINT       NULL REFERENCES kundali_snapshot(id) ON DELETE SET NULL,
    matching_id                 BIGINT       NULL REFERENCES matching_session(id) ON DELETE SET NULL,
    display_title               VARCHAR(256) NOT NULL,
    storage_path                VARCHAR(1024) NOT NULL,
    file_size_bytes             BIGINT       NOT NULL,
    content_type                VARCHAR(64)  NOT NULL DEFAULT 'application/pdf',
    calculation_engine_version  VARCHAR(16)  NOT NULL,
    generated_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_kundali_report_type CHECK (
        report_type IN ('BASIC_KUNDALI', 'MATCHING', 'DASHA_SUMMARY', 'TRANSIT')
    ),
    CONSTRAINT ck_kundali_report_source CHECK (
        (report_type = 'MATCHING' AND matching_id IS NOT NULL)
        OR (report_type <> 'MATCHING' AND kundali_id IS NOT NULL)
    )
);

CREATE INDEX idx_kundali_report_tenant ON kundali_report (tenant_id);
CREATE INDEX idx_kundali_report_tenant_generated ON kundali_report (tenant_id, generated_at DESC);
CREATE INDEX idx_kundali_report_kundali ON kundali_report (kundali_id);
CREATE INDEX idx_kundali_report_matching ON kundali_report (matching_id);
