package br.dev.irontech.seavault.documents.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.expiry.ExpiryStatus;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.documents.dto.DocumentRequest;
import br.dev.irontech.seavault.documents.dto.DocumentResponse;
import br.dev.irontech.seavault.reference.domain.RefType;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class DocumentServiceTest {

    @Inject
    DocumentService documentService;

    @Inject
    UserRepository userRepository;

    @Inject
    ReferenceRepository referenceRepository;

    @Inject
    EntityManager em;

    @Transactional
    UUID newUser(String email) {
        User u = new User();
        u.name = "Dono";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u.id;
    }

    private UUID documentTypeId() {
        return referenceRepository.listTypes("DOCUMENT").get(0).id;
    }

    @Transactional
    UUID newType(String kind, String code) {
        RefType type = new RefType();
        type.id = UUID.randomUUID();
        type.kind = kind;
        type.code = code;
        type.label = code;
        em.persist(type);
        return type.id;
    }

    private DocumentRequest req(UUID typeId, LocalDate expiry) {
        return new DocumentRequest(typeId, "REG-1", "Marinha", LocalDate.of(2020, 1, 1), expiry, "obs");
    }

    @Test
    void createPersistsAndDerivesStatus() {
        UUID userId = newUser("doc-svc-create@example.com");
        DocumentResponse resp = documentService.create(userId, req(documentTypeId(), LocalDate.of(2020, 1, 1)));

        assertNotNull(resp.id());
        assertEquals("REG-1", resp.number());
        assertEquals(ExpiryStatus.VENCIDO, resp.status());
    }

    @Test
    void createWithUnknownTypeReturns404() {
        UUID userId = newUser("doc-svc-badtype@example.com");
        UUID unknown = UUID.fromString("00000000-0000-0000-0000-000000000000");
        assertThrows(NotFoundException.class, () -> documentService.create(userId, req(unknown, null)));
    }

    @Test
    void createWithNonDocumentTypeReturns404() {
        UUID userId = newUser("doc-svc-wrongkind@example.com");
        UUID vesselType = newType("VESSEL", "BARCO_TESTE");

        assertThrows(NotFoundException.class, () -> documentService.create(userId, req(vesselType, null)));
    }

    @Test
    void nullExpiryYieldsSemValidade() {
        UUID userId = newUser("doc-svc-noexpiry@example.com");
        DocumentResponse resp = documentService.create(userId, req(documentTypeId(), null));
        assertEquals(ExpiryStatus.SEM_VALIDADE, resp.status());
    }

    @Test
    void updateReplacesFields() {
        UUID userId = newUser("doc-svc-update@example.com");
        UUID type = documentTypeId();
        DocumentResponse created = documentService.create(userId, req(type, null));

        DocumentResponse updated = documentService.update(userId, created.id(),
                new DocumentRequest(type, "REG-2", "Capitania", null, null, null));

        assertEquals("REG-2", updated.number());
        assertEquals("Capitania", updated.issuer());
    }

    @Test
    void getOfAnotherUsersDocumentReturns404() {
        UUID a = newUser("doc-svc-iso-a@example.com");
        UUID b = newUser("doc-svc-iso-b@example.com");
        DocumentResponse d = documentService.create(a, req(documentTypeId(), null));

        assertThrows(NotFoundException.class, () -> documentService.get(b, d.id()));
    }

    @Test
    void deleteMakesItUnreadable() {
        UUID userId = newUser("doc-svc-delete@example.com");
        DocumentResponse d = documentService.create(userId, req(documentTypeId(), null));

        documentService.delete(userId, d.id());

        assertThrows(NotFoundException.class, () -> documentService.get(userId, d.id()));
        assertEquals(0, documentService.list(userId, PageRequest.of(0, 20)).content().size());
    }
}
