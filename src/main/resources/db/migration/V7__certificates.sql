-- Certificados profissionais do aquaviario (codigo/regra livre, sem FK de catalogo).
CREATE TABLE certificates (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id),
    name        VARCHAR(160) NOT NULL,
    code        VARCHAR(80),
    institution VARCHAR(160),
    issue_date  DATE,
    expiry_date DATE,
    notes       VARCHAR(2000),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX ix_certificates_user_active ON certificates (user_id) WHERE deleted_at IS NULL;
