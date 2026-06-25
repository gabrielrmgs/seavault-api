-- Embarcacoes do aquaviario (privadas por usuario no MVP). type_id referencia ref_types (kind VESSEL), opcional.
CREATE TABLE vessels (
    id             UUID PRIMARY KEY,
    user_id        UUID          NOT NULL REFERENCES users (id),
    type_id        UUID          REFERENCES ref_types (id),
    name           VARCHAR(160)  NOT NULL,
    imo            VARCHAR(20),
    flag           VARCHAR(80),
    gross_tonnage  NUMERIC(12,2),
    notes          VARCHAR(2000),
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL,
    deleted_at     TIMESTAMPTZ
);

CREATE INDEX ix_vessels_user_active ON vessels (user_id) WHERE deleted_at IS NULL;
