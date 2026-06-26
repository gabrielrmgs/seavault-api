-- Historico append-only de geracao de relatorios (sem soft delete; registro de auditoria).
CREATE TABLE report_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users (id),
    type         VARCHAR(30)  NOT NULL,   -- CAREER | SEATIME | CERTIFICATES | DOCUMENTS | CV
    format       VARCHAR(10)  NOT NULL,   -- JSON | PDF
    params       TEXT,
    generated_at TIMESTAMPTZ  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_report_history_user ON report_history (user_id, generated_at DESC);
