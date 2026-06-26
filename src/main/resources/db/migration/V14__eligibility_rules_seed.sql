-- ===== Regras exemplares (taxonomia MARITIMOS, conves). Numeros ilustrativos, refinar com regras oficiais. =====

-- Regra 1: progressao para Moco de Conves
INSERT INTO ref_eligibility_rules (code, name, target_category_id)
SELECT 'PROG_MOCO_CONVES', 'Progressao para Moco de Conves', c.id
FROM ref_categories c WHERE c.code = 'MOCO_CONVES';

-- Regra 2: progressao para Marinheiro de Conves
INSERT INTO ref_eligibility_rules (code, name, target_category_id)
SELECT 'PROG_MARINHEIRO_CONVES', 'Progressao para Marinheiro de Conves', c.id
FROM ref_categories c WHERE c.code = 'MARINHEIRO_CONVES';

-- Regra 3: habilitacao para o curso CACI (alvo = curso do catalogo)
INSERT INTO ref_eligibility_rules (code, name, target_course_id)
SELECT 'HABILITA_CACI', 'Habilitacao para o curso CACI', cc.id
FROM ref_course_catalog cc WHERE cc.code = 'CACI';

-- ===== Requisitos da Regra 1 (Moco de Conves): categoria min Mar.Aux.Conves + 180 dias + curso CBSP =====
INSERT INTO ref_eligibility_requirements (rule_id, requirement_type, required_category_id, display_order)
SELECT r.id, 'CATEGORY', cat.id, 1
FROM ref_eligibility_rules r, ref_categories cat
WHERE r.code = 'PROG_MOCO_CONVES' AND cat.code = 'MAR_AUX_CONVES';

INSERT INTO ref_eligibility_requirements (rule_id, requirement_type, required_days, display_order)
SELECT r.id, 'SEATIME', 180, 2
FROM ref_eligibility_rules r WHERE r.code = 'PROG_MOCO_CONVES';

INSERT INTO ref_eligibility_requirements (rule_id, requirement_type, required_course_id, display_order)
SELECT r.id, 'COURSE', cc.id, 3
FROM ref_eligibility_rules r, ref_course_catalog cc
WHERE r.code = 'PROG_MOCO_CONVES' AND cc.code = 'CBSP';

-- ===== Requisitos da Regra 2 (Marinheiro de Conves): categoria min Moco de Conves + 365 dias + curso CFAQ =====
INSERT INTO ref_eligibility_requirements (rule_id, requirement_type, required_category_id, display_order)
SELECT r.id, 'CATEGORY', cat.id, 1
FROM ref_eligibility_rules r, ref_categories cat
WHERE r.code = 'PROG_MARINHEIRO_CONVES' AND cat.code = 'MOCO_CONVES';

INSERT INTO ref_eligibility_requirements (rule_id, requirement_type, required_days, display_order)
SELECT r.id, 'SEATIME', 365, 2
FROM ref_eligibility_rules r WHERE r.code = 'PROG_MARINHEIRO_CONVES';

INSERT INTO ref_eligibility_requirements (rule_id, requirement_type, required_course_id, display_order)
SELECT r.id, 'COURSE', cc.id, 3
FROM ref_eligibility_rules r, ref_course_catalog cc
WHERE r.code = 'PROG_MARINHEIRO_CONVES' AND cc.code = 'CFAQ';

-- ===== Requisitos da Regra 3 (CACI): curso STCW_BST concluido =====
INSERT INTO ref_eligibility_requirements (rule_id, requirement_type, required_course_id, display_order)
SELECT r.id, 'COURSE', cc.id, 1
FROM ref_eligibility_rules r, ref_course_catalog cc
WHERE r.code = 'HABILITA_CACI' AND cc.code = 'STCW_BST';
