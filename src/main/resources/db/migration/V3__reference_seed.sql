-- ===== Grupos profissionais =====
INSERT INTO ref_professional_groups (code, name, display_order) VALUES
  ('MARITIMOS',    'Marítimos',     1),
  ('FLUVIARIOS',   'Fluviários',    2),
  ('PESCADORES',   'Pescadores',    3),
  ('MERGULHADORES','Mergulhadores', 4),
  ('PRATICOS',     'Práticos',      5);

-- ===== Categorias (Marítimos — convés e máquinas, em ordem de progressão) =====
INSERT INTO ref_categories (group_id, code, name, progression_order)
SELECT g.id, c.code, c.name, c.progression_order
FROM (VALUES
    ('MAR_AUX_CONVES',     'Marinheiro Auxiliar de Convés',   1),
    ('MAR_AUX_MAQUINAS',   'Marinheiro Auxiliar de Máquinas', 1),
    ('MOCO_CONVES',        'Moço de Convés',                  2),
    ('MOCO_MAQUINAS',      'Moço de Máquinas',                2),
    ('MARINHEIRO_CONVES',  'Marinheiro de Convés',            3),
    ('MARINHEIRO_MAQUINAS','Marinheiro de Máquinas',          3),
    ('CONTRAMESTRE',       'Contramestre',                    4),
    ('OFICIAL_NAUTICA',    'Oficial de Náutica',              5),
    ('OFICIAL_MAQUINAS',   'Oficial de Máquinas',             5)
) AS c(code, name, progression_order)
CROSS JOIN ref_professional_groups g
WHERE g.code = 'MARITIMOS';

-- ===== Categorias (Pescadores) =====
INSERT INTO ref_categories (group_id, code, name, progression_order)
SELECT g.id, c.code, c.name, c.progression_order
FROM (VALUES
    ('PESCADOR_PROFISSIONAL', 'Pescador Profissional', 1),
    ('MESTRE_PESCA',          'Mestre de Pesca',       2)
) AS c(code, name, progression_order)
CROSS JOIN ref_professional_groups g
WHERE g.code = 'PESCADORES';

-- ===== Catálogo de cursos =====
INSERT INTO ref_course_catalog (code, name, workload_hours) VALUES
  ('STCW_BST',  'Treinamento Básico de Segurança (Basic Safety Training)', 40),
  ('CBSP',      'Curso Básico de Segurança Pessoal e Responsabilidade Social', 24),
  ('CFAQ',      'Curso de Formação de Aquaviários', 200),
  ('ESPA',      'Curso Especial de Primeiros Socorros e Atendimento Pré-Hospitalar', 30),
  ('CACI',      'Curso de Atualização para Combate a Incêndio', 16);

-- ===== Tipos (DOCUMENT / VESSEL / COMPANY) =====
INSERT INTO ref_types (kind, code, label) VALUES
  ('DOCUMENT', 'CIR',       'Caderneta de Inscrição e Registro'),
  ('DOCUMENT', 'CIM',       'Certificado Internacional de Marinheiro'),
  ('DOCUMENT', 'PASSAPORTE','Passaporte'),
  ('DOCUMENT', 'ASO',       'Atestado de Saúde Ocupacional'),
  ('DOCUMENT', 'VISTO',     'Visto'),
  ('VESSEL',   'CARGUEIRO', 'Navio de Carga'),
  ('VESSEL',   'PETROLEIRO','Navio Petroleiro'),
  ('VESSEL',   'AHTS',      'Embarcação de Apoio (AHTS)'),
  ('VESSEL',   'PESQUEIRO', 'Embarcação de Pesca'),
  ('COMPANY',  'ARMADORA',  'Empresa Armadora'),
  ('COMPANY',  'AGENCIA',   'Agência de Tripulação'),
  ('COMPANY',  'ESTALEIRO', 'Estaleiro');
