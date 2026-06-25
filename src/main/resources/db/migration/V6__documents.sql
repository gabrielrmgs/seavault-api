-- Documentos do aquaviario (CIR, ASO, passaporte, visto, ...). type_id referencia ref_types (kind DOCUMENT).
CREATE TABLE documents (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id),
    type_id     UUID         NOT NULL REFERENCES ref_types (id),
    number      VARCHAR(80),
    issuer      VARCHAR(160),
    issue_date  DATE,
    expiry_date DATE,
    notes       VARCHAR(2000),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX ix_documents_user_active ON documents (user_id) WHERE deleted_at IS NULL;
