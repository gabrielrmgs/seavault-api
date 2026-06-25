package br.dev.irontech.seavault.files.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.files.domain.StoredFile;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class FileRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FileRepository fileRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Arquivo";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private StoredFile persistFile(User owner, String name) {
        StoredFile f = new StoredFile();
        f.userId = owner.id;
        f.originalName = name;
        f.contentType = "application/pdf";
        f.sizeBytes = 10;
        f.storageKey = owner.id + "/" + name;
        f.sha256 = "deadbeef";
        fileRepository.persist(f);
        return f;
    }

    @Test
    @Transactional
    void findActiveByIdAndUserRespectsOwnership() {
        User a = persistUser("file-owner-a@example.com");
        User b = persistUser("file-owner-b@example.com");
        StoredFile f = persistFile(a, "cir.pdf");

        assertTrue(fileRepository.findActiveByIdAndUser(f.id, a.id).isPresent());
        assertFalse(fileRepository.findActiveByIdAndUser(f.id, b.id).isPresent());
    }

    @Test
    @Transactional
    void ignoresSoftDeletedFile() {
        User a = persistUser("file-deleted@example.com");
        StoredFile f = persistFile(a, "old.pdf");
        f.deletedAt = Instant.now();
        fileRepository.persist(f);

        assertFalse(fileRepository.findActiveByIdAndUser(f.id, a.id).isPresent());
    }

    @Test
    @Transactional
    void listAndCountAreScopedToUser() {
        User a = persistUser("file-list@example.com");
        persistFile(a, "a1.pdf");
        persistFile(a, "a2.pdf");

        assertEquals(2L, fileRepository.countActiveByUser(a.id));
        assertEquals(2, fileRepository.listActiveByUser(a.id, PageRequest.of(0, 20)).size());
    }
}
