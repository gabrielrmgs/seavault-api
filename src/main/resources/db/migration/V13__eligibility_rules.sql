-- Regras de elegibilidade data-driven: cada regra tem um alvo (categoria OU curso do catalogo)
-- e uma lista de requisitos. O motor (modulo eligibility) so le estas tabelas.
CREATE TABLE ref_eligibility_rules (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code               VARCHAR(60)  NOT NULL UNIQUE,
    name               VARCHAR(160) NOT NULL,
    target_category_id UUID REFERENCES ref_categories (id),
    target_course_id   UUID REFERENCES ref_course_catalog (id),
    CONSTRAINT ck_eligibility_rule_target CHECK (
        (target_category_id IS NOT NULL AND target_course_id IS NULL)
        OR (target_category_id IS NULL AND target_course_id IS NOT NULL)
    )
);

-- Itens de requisito de uma regra. requirement_type define qual coluna de alvo se aplica:
--   COURSE   -> required_course_id   (curso do catalogo que deve estar CONCLUIDO)
--   SEATIME  -> required_days        (dias minimos de mar)
--   CATEGORY -> required_category_id (categoria minima por progression_order)
CREATE TABLE ref_eligibility_requirements (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id              UUID        NOT NULL REFERENCES ref_eligibility_rules (id),
    requirement_type     VARCHAR(20) NOT NULL,
    required_course_id   UUID REFERENCES ref_course_catalog (id),
    required_category_id UUID REFERENCES ref_categories (id),
    required_days        INTEGER,
    display_order        INTEGER     NOT NULL DEFAULT 0
);

CREATE INDEX ix_eligibility_requirements_rule ON ref_eligibility_requirements (rule_id);
