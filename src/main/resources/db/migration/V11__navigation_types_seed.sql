-- Tipos de navegacao (areas de navegacao do aquaviario). Kind NAVIGATION na taxonomia ref_types.
INSERT INTO ref_types (kind, code, label) VALUES
  ('NAVIGATION', 'MAR_ABERTO',      'Navegação em Mar Aberto'),
  ('NAVIGATION', 'INTERIOR',        'Navegação Interior'),
  ('NAVIGATION', 'APOIO_MARITIMO',  'Navegação de Apoio Marítimo'),
  ('NAVIGATION', 'APOIO_PORTUARIO', 'Navegação de Apoio Portuário');
