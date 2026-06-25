package br.dev.irontech.seavault.files.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.files.domain.OwnerType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class FileLinkServiceTest {

    @Inject
    FileService fileService;

    @Inject
    UserRepository userRepository;

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

    private UUID upload(UUID userId, String name) {
        return fileService.upload(userId, name, "application/pdf",
                ("%PDF-1.4 " + name).getBytes(StandardCharsets.UTF_8)).id();
    }

    @Test
    void linkThenFilesForOwnerReturnsFile() {
        UUID userId = newUser("link-basic@example.com");
        UUID fileId = upload(userId, "frente.pdf");
        UUID ownerId = UUID.randomUUID();

        fileService.link(userId, fileId, OwnerType.DOCUMENT, ownerId);

        var files = fileService.filesForOwner(userId, OwnerType.DOCUMENT, ownerId);
        assertEquals(1, files.size());
        assertEquals(fileId, files.get(0).id());
    }

    @Test
    void linkIsIdempotent() {
        UUID userId = newUser("link-idem@example.com");
        UUID fileId = upload(userId, "f.pdf");
        UUID ownerId = UUID.randomUUID();

        fileService.link(userId, fileId, OwnerType.CERTIFICATE, ownerId);
        fileService.link(userId, fileId, OwnerType.CERTIFICATE, ownerId);

        assertEquals(1, fileService.filesForOwner(userId, OwnerType.CERTIFICATE, ownerId).size());
    }

    @Test
    void unlinkRemovesFromOwner() {
        UUID userId = newUser("unlink@example.com");
        UUID fileId = upload(userId, "f.pdf");
        UUID ownerId = UUID.randomUUID();
        fileService.link(userId, fileId, OwnerType.COURSE, ownerId);

        fileService.unlink(userId, fileId, OwnerType.COURSE, ownerId);

        assertEquals(0, fileService.filesForOwner(userId, OwnerType.COURSE, ownerId).size());
    }

    @Test
    void unlinkAllClearsEveryLinkOfOwner() {
        UUID userId = newUser("unlinkall@example.com");
        UUID ownerId = UUID.randomUUID();
        fileService.link(userId, upload(userId, "a.pdf"), OwnerType.DOCUMENT, ownerId);
        fileService.link(userId, upload(userId, "b.pdf"), OwnerType.DOCUMENT, ownerId);

        fileService.unlinkAll(OwnerType.DOCUMENT, ownerId);

        assertEquals(0, fileService.filesForOwner(userId, OwnerType.DOCUMENT, ownerId).size());
    }

    @Test
    void linkingAnotherUsersFileReturns404() {
        UUID a = newUser("link-iso-a@example.com");
        UUID b = newUser("link-iso-b@example.com");
        UUID fileOfA = upload(a, "secret.pdf");
        UUID ownerId = UUID.randomUUID();

        assertThrows(NotFoundException.class,
                () -> fileService.link(b, fileOfA, OwnerType.DOCUMENT, ownerId));
    }

    @Test
    void filesForOwnerOnlyReturnsCallersFiles() {
        UUID a = newUser("foo-a@example.com");
        UUID b = newUser("foo-b@example.com");
        UUID sharedOwner = UUID.randomUUID();
        UUID fileOfA = upload(a, "a.pdf");
        fileService.link(a, fileOfA, OwnerType.DOCUMENT, sharedOwner);

        assertEquals(0, fileService.filesForOwner(b, OwnerType.DOCUMENT, sharedOwner).size());
    }
}
