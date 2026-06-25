CREATE TABLE users (
    id              UUID PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    plan            VARCHAR(20)  NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    terms_accepted_at TIMESTAMPTZ,
    locale          VARCHAR(10)  NOT NULL DEFAULT 'pt-BR',
    country         VARCHAR(2)   NOT NULL DEFAULT 'BR',
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    deleted_at      TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_users_email_active ON users (email) WHERE deleted_at IS NULL;

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users (id),
    token_hash  VARCHAR(64) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_refresh_tokens_hash ON refresh_tokens (token_hash);

CREATE TABLE email_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users (id),
    type        VARCHAR(20) NOT NULL,
    token_hash  VARCHAR(64) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_email_tokens_hash ON email_tokens (token_hash);
