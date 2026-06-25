-- Cursos do aquaviario. catalog_course_id referencia ref_course_catalog (opcional; curso fora do catalogo permitido).
-- status: PLANEJADO / EM_ANDAMENTO / CONCLUIDO (definido pelo usuario).
CREATE TABLE courses (
    id                UUID PRIMARY KEY,
    user_id           UUID         NOT NULL REFERENCES users (id),
    name              VARCHAR(160) NOT NULL,
    catalog_course_id UUID         REFERENCES ref_course_catalog (id),
    institution       VARCHAR(160),
    modality          VARCHAR(40),
    workload_hours    INT,
    start_date        DATE,
    completion_date   DATE,
    status            VARCHAR(20)  NOT NULL,
    notes             VARCHAR(2000),
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    deleted_at        TIMESTAMPTZ
);

CREATE INDEX ix_courses_user_active ON courses (user_id) WHERE deleted_at IS NULL;
