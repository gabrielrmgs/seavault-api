-- Grupos profissionais aquaviários (Marítimos, Fluviários, Pescadores, ...)
CREATE TABLE ref_professional_groups (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code          VARCHAR(40)  NOT NULL UNIQUE,
    name          VARCHAR(120) NOT NULL,
    display_order INT          NOT NULL DEFAULT 0
);

-- Categorias / níveis de progressão, pertencentes a um grupo
CREATE TABLE ref_categories (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id          UUID         NOT NULL REFERENCES ref_professional_groups (id),
    code              VARCHAR(40)  NOT NULL UNIQUE,
    name              VARCHAR(120) NOT NULL,
    progression_order INT          NOT NULL DEFAULT 0
);

CREATE INDEX ix_ref_categories_group ON ref_categories (group_id);

-- Catálogo oficial de cursos
CREATE TABLE ref_course_catalog (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code           VARCHAR(40)  NOT NULL UNIQUE,
    name           VARCHAR(160) NOT NULL,
    workload_hours INT
);

-- Tipos de referência consolidados (DOCUMENT / VESSEL / COMPANY)
CREATE TABLE ref_types (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kind  VARCHAR(20)  NOT NULL,
    code  VARCHAR(40)  NOT NULL,
    label VARCHAR(120) NOT NULL,
    CONSTRAINT ux_ref_types_kind_code UNIQUE (kind, code)
);

CREATE INDEX ix_ref_types_kind ON ref_types (kind);
