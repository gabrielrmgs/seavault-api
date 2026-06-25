package br.dev.irontech.seavault.documents.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.documents.domain.Document;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class DocumentRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    ReferenceRepository referenceRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Doc";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private UUID anyDocumentTypeId() {
        return referenceRepository.listTypes("DOCUMENT").get(0).id;
    }

    private Document persistDoc(User owner, UUID typeId) {
        Document d = new Document();
        d.userId = owner.id;
        d.typeId = typeId;
        d.number = "123";
        documentRepository.persist(d);
        return d;
    }

    @Test
    @Transactional
    void findActiveByIdAndUserRespectsOwnership() {
        User a = persistUser("doc-a@example.com");
        User b = persistUser("doc-b@example.com");
        Document d = persistDoc(a, anyDocumentTypeId());

        assertTrue(documentRepository.findActiveByIdAndUser(d.id, a.id).isPresent());
        assertFalse(documentRepository.findActiveByIdAndUser(d.id, b.id).isPresent());
    }

    @Test
    @Transactional
    void ignoresSoftDeleted() {
        User a = persistUser("doc-del@example.com");
        Document d = persistDoc(a, anyDocumentTypeId());
        d.deletedAt = Instant.now();
        documentRepository.persist(d);

        assertFalse(documentRepository.findActiveByIdAndUser(d.id, a.id).isPresent());
    }

    @Test
    @Transactional
    void listAndCountScopedToUser() {
        User a = persistUser("doc-list@example.com");
        UUID type = anyDocumentTypeId();
        persistDoc(a, type);
        persistDoc(a, type);

        assertEquals(2L, documentRepository.countActiveByUser(a.id));
        assertEquals(2, documentRepository.listActiveByUser(a.id, PageRequest.of(0, 20)).size());
    }
}
