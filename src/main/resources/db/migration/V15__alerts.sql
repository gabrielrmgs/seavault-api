-- Alertas gerados pelo job diario (1 por entidade-origem, via chave unica). Sem soft delete: ciclo via status.
CREATE TABLE alerts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users (id),
    source      VARCHAR(20)  NOT NULL,   -- DOCUMENT | CERTIFICATE | COURSE | VOYAGE
    source_id   UUID         NOT NULL,
    type        VARCHAR(30)  NOT NULL,   -- DOCUMENT_EXPIRY | CERTIFICATE_EXPIRY | COURSE_START | VOYAGE_REMINDER
    title       VARCHAR(200) NOT NULL,
    due_date    DATE,
    lead_days   INTEGER,
    status      VARCHAR(20)  NOT NULL,   -- PENDENTE | LIDO | RESOLVIDO | IGNORADO
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    resolved_at TIMESTAMPTZ,
    CONSTRAINT ux_alerts_source UNIQUE (user_id, source, source_id)
);

CREATE INDEX ix_alerts_user_status ON alerts (user_id, status);
CREATE INDEX ix_alerts_open ON alerts (status);
