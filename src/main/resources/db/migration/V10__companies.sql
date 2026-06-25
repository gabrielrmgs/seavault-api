-- Empresas (armadoras, agencias, estaleiros) do aquaviario. type_id referencia ref_types (kind COMPANY), opcional.
CREATE TABLE companies (
    id          UUID PRIMARY KEY,
    user_id     UUID          NOT NULL REFERENCES users (id),
    type_id     UUID          REFERENCES ref_types (id),
    name        VARCHAR(160)  NOT NULL,
    cnpj        VARCHAR(18),
    email       VARCHAR(160),
    phone       VARCHAR(40),
    notes       VARCHAR(2000),
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL,
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX ix_companies_user_active ON companies (user_id) WHERE deleted_at IS NULL;
