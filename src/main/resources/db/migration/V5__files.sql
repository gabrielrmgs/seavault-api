-- Metadado de arquivos do usuario (o byte vive no storage, nao no banco)
CREATE TABLE files (
    id            UUID PRIMARY KEY,
    user_id       UUID         NOT NULL REFERENCES users (id),
    original_name VARCHAR(255) NOT NULL,
    content_type  VARCHAR(120) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    storage_key   VARCHAR(255) NOT NULL,
    sha256        VARCHAR(64)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    deleted_at    TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_files_storage_key ON files (storage_key);
CREATE INDEX ix_files_user_active ON files (user_id) WHERE deleted_at IS NULL;

-- Vinculo polimorfico arquivo -> entidade dona (documento/certificado/curso/embarque)
CREATE TABLE file_links (
    id         UUID PRIMARY KEY,
    file_id    UUID        NOT NULL REFERENCES files (id),
    owner_type VARCHAR(20) NOT NULL,
    owner_id   UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ
);

CREATE INDEX ix_file_links_owner ON file_links (owner_type, owner_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_file_links_file ON file_links (file_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX ux_file_links_active ON file_links (file_id, owner_type, owner_id) WHERE deleted_at IS NULL;
