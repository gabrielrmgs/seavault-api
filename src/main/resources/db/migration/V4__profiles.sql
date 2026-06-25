CREATE TABLE profiles (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users (id),
    cir                 VARCHAR(40),
    cpf                 VARCHAR(14),
    rg                  VARCHAR(20),
    nationality         VARCHAR(60),
    phone               VARCHAR(20),
    emergency_contact   VARCHAR(160),
    category_id         UUID REFERENCES ref_categories (id),
    target_category_id  UUID REFERENCES ref_categories (id),
    completion_percent  INT  NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    deleted_at          TIMESTAMPTZ
);

-- 1:1: no máximo um perfil vivo por usuário
CREATE UNIQUE INDEX ux_profiles_user_active ON profiles (user_id) WHERE deleted_at IS NULL;
