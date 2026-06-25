package br.dev.irontech.seavault.files.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.files.dto.FileDownload;
import br.dev.irontech.seavault.files.dto.FileResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class FileServiceTest {

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

    private byte[] pdf(String body) {
        return ("%PDF-1.4 " + body).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void uploadPersistsMetadataAndStoresBytes() {
        UUID userId = newUser("svc-upload@example.com");
        byte[] content = pdf("ola");

        FileResponse resp = fileService.upload(userId, "cir.pdf", "application/pdf", content);

        assertNotNull(resp.id());
        assertEquals("cir.pdf", resp.originalName());
        assertEquals("application/pdf", resp.contentType());
        assertEquals(content.length, resp.sizeBytes());
        assertNotNull(resp.sha256());

        FileDownload dl = fileService.download(userId, resp.id());
        assertArrayEquals(content, dl.content());
        assertEquals("application/pdf", dl.contentType());
    }

    @Test
    void uploadRejectsEmptyFile() {
        UUID userId = newUser("svc-empty@example.com");
        assertThrows(BusinessException.class,
                () -> fileService.upload(userId, "x.pdf", "application/pdf", new byte[0]));
    }

    @Test
    void uploadRejectsOversizeFile() {
        UUID userId = newUser("svc-oversize@example.com");
        byte[] big = new byte[2048];
        assertThrows(BusinessException.class,
                () -> fileService.upload(userId, "big.pdf", "application/pdf", big));
    }

    @Test
    void uploadRejectsDisallowedContentType() {
        UUID userId = newUser("svc-mime@example.com");
        assertThrows(BusinessException.class,
                () -> fileService.upload(userId, "x.txt", "text/plain", pdf("z")));
    }

    @Test
    void getReturnsMetadataForOwnedFile() {
        UUID userId = newUser("svc-get@example.com");
        FileResponse f = fileService.upload(userId, "meta.pdf", "application/pdf", pdf("m"));

        FileResponse found = fileService.get(userId, f.id());

        assertEquals(f.id(), found.id());
        assertEquals("meta.pdf", found.originalName());
        assertEquals("application/pdf", found.contentType());
    }

    @Test
    void downloadOfAnotherUsersFileReturns404() {
        UUID a = newUser("svc-iso-a@example.com");
        UUID b = newUser("svc-iso-b@example.com");
        FileResponse f = fileService.upload(a, "secret.pdf", "application/pdf", pdf("s"));

        assertThrows(NotFoundException.class, () -> fileService.download(b, f.id()));
    }

    @Test
    void deleteMakesFileUnreadable() {
        UUID userId = newUser("svc-delete@example.com");
        FileResponse f = fileService.upload(userId, "tmp.pdf", "application/pdf", pdf("t"));

        fileService.delete(userId, f.id());

        assertThrows(NotFoundException.class, () -> fileService.download(userId, f.id()));
        assertEquals(0, fileService.list(userId, PageRequest.of(0, 20)).content().size());
    }
}
