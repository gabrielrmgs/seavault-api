-- Embarques/desembarques do aquaviario (privados por usuario). FKs opcionais para vessels, companies e ref_types/ref_categories.
CREATE TABLE voyages (
    id                  UUID PRIMARY KEY,
    user_id             UUID          NOT NULL REFERENCES users (id),
    vessel_id           UUID          REFERENCES vessels (id),
    company_id          UUID          REFERENCES companies (id),
    navigation_type_id  UUID          REFERENCES ref_types (id),
    category_id         UUID          REFERENCES ref_categories (id),
    role                VARCHAR(120),
    embark_date         DATE          NOT NULL,
    disembark_date      DATE,
    embark_port         VARCHAR(120),
    disembark_port      VARCHAR(120),
    calculated_days     INTEGER,
    computed_days       INTEGER,
    override_reason     VARCHAR(500),
    overridden_at       TIMESTAMPTZ,
    notes               VARCHAR(2000),
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX ix_voyages_user_active ON voyages (user_id) WHERE deleted_at IS NULL;
