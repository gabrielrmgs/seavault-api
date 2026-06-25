# SeaVault Plano 1 — Fundação & Autenticação

Branch: feat/foundation-auth
Base: d18c719 (Initial commit)

Task 1: complete (commits d18c719..97757a5, review clean)
Task 2: complete (commits 97757a5..a08c4e7, review clean)
Task 3: complete (commits a08c4e7..72dcc43, review clean)
  WATCH: validation-mapper precedence to be confirmed by Task 15 e2e (expects code=VALIDATION on 400)
Task 4: complete (commits 72dcc43..28ae912, review clean)
Task 5: complete (commits 28ae912..2e6d8fc, review clean)
Task 6: complete (commits 2e6d8fc..73711ca, review clean)
Task 7: complete (commits 73711ca..73543ad, review clean)
  MINOR: UserRepositoryTest doesn't cover soft-deleted user filtering (deletedAt is null path untested) — triage at final review
Task 8: complete (commits 73543ad..77fe991, review clean; test deviation: FK requires real User — sound fix)
  MINOR: @Transactional auth tests commit (not rollback) with hardcoded emails — relies on fresh Testcontainer DB per run; triage at final review
Task 9: complete (commits 77fe991..a0b4d0f, review clean)
  MINOR: sendPasswordReset has no direct unit test (per brief); exercised e2e in Task 14 — triage at final review
Task 10: complete (commits a0b4d0f..63cb25d, review clean)
Task 11: complete (commits 63cb25d..3e3439b, review clean)
Task 12: complete (commits 3e3439b..676ecc7, review clean)
Task 13: complete (commits 676ecc7..be482a1, review clean)
Task 14: complete (commits be482a1..b0c402d, review clean)
Task 15: complete (commits b0c402d..196b63d, review clean; FULL SUITE: 22 tests, 0 failures)
  MINOR: e2e tests not isolated/cleaned across repeated runs vs same DB (dup of Task 8 minor; fresh Testcontainer per run mitigates)

PLAN 1 COMPLETE — all 15 tasks done, suite green (22 tests).

FIX WAVE: complete (commit 196b63d..300986d, re-review clean) — JWT key externalized+rotated+untracked; soft-delete guard added in confirmEmail/resetPassword; suite 23 green.
  MINOR (deferred): confirmEmail soft-delete guard has no dedicated test.
PLAN 1 FINAL STATE: 15 tasks + fix wave done, suite 23/0/0, branch feat/foundation-auth ready to finish.

# SeaVault Plano 2 — Referência & Perfil

Branch: feat/reference-profile
Base: 300986d (main after Plan 1 fix wave)

Task 1: complete (commits 300986d..8e6d133, review clean)
Task 2: complete (commits 8e6d133..3cf7307, review clean)
Task 3: complete (commits 3cf7307..c66154b, review clean)
Task 4: complete (commits c66154b..0ce870f, review clean)
Task 5: complete (commits 0ce870f..87813d1, review clean)
Task 6: complete (commits 87813d1..b9cc06a, review clean)
Task 7: complete (commits b9cc06a..6be131f, review clean)
Task 8: complete (commits 6be131f..1c01fe9, review clean)
Task 9: complete (commits 1c01fe9..c42ae71, review clean)
  MINOR: ProfileService.update usa semantica replace-all (nao patch) — omitir campo no body apaga o dado. Dentro do spec; triage no review final.
Task 10: complete (commits c42ae71..177ba29, review clean; compile-only verification, e2e em Task 11)
Task 11: complete (commits 177ba29..a7086f9, review clean; FULL SUITE: 55 tests, 0 failures, 0 errors)

PLAN 2 COMPLETE — all 11 tasks done, suite green (55 tests).

FINAL REVIEW: clean (review-300986d..a7086f9)
  MINOR (aceito): ProfileCompletion — excluir targetCategoryId da pontuação e silencioso; adicionar comentario inline
  MINOR (aceito): CPF @Size(min=11,max=14) permite comprimentos 12-13 invalidos; backlog validacao CPF
  MINOR (aceito): V3 seed — 3 de 5 grupos sem categorias (FLUVIARIOS, MERGULHADORES, PRATICOS); backlog seed
  MINOR (aceito): CurrentUser — UUID.fromString lancar IAE (500) se subject nao for UUID valido; hardening futuro
  ACEITO: replace-all semantics do PUT e correto por HTTP; documentar no contrato da API
PLAN 2 FINAL STATE: 11 tasks + review final clean, suite 55/0/0, branch feat/reference-profile pronta.

# SeaVault Plano 3a — Files & Anexos

Branch: feat/files
Base: a7086f9 (main after Plan 2)

Task 1: complete (commit 712ba49, review clean; RED->GREEN FilesSchemaBootTest)
Task 2: complete (commit faf57dd, review found path traversal risk)
  FIX: complete (commit 310c33f, LocalFileStorage rejects storage keys outside base dir; LocalFileStorageTest 4 green)
Task 3: complete (commit 664be89, review clean)
Task 4: complete (commit b2ac371, review found resource access contract issue)
  FIX: complete (commit 4773211, FileService exposes public get(userId, fileId); files tests 15 green)
Task 5: complete (commit 5f155d6, review clean)
Task 6: complete (commit 1218bd0, review clean; FileResourceTest 7 green)
Task 7: complete (full suite: 83 tests, 0 failures, 0 errors)

PLAN 3a COMPLETE — files module delivered: schema, local storage, domain/repositories, service upload/download/list/delete, attachment API, REST endpoints, suite green (83/0/0).
