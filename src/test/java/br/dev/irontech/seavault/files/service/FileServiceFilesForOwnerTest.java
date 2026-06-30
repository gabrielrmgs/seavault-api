package br.dev.irontech.seavault.files.service;

import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.service.AuthService;
import br.dev.irontech.seavault.files.domain.OwnerType;
import br.dev.irontech.seavault.files.dto.FileResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class FileServiceFilesForOwnerTest {

    @Inject
    FileService fileService;

    @Inject
    AuthService authService;

    @Test
    void returnsAllLinkedFilesForOwner() {
        UUID userId = authService.register(
                new RegisterRequest("Files Owner", "files-owner@example.com", "senha12345", true)).id();
        UUID ownerId = UUID.randomUUID();
        FileResponse a = fileService.upload(userId, "a.pdf", "application/pdf", "AAA".getBytes());
        FileResponse b = fileService.upload(userId, "b.pdf", "application/pdf", "BBB".getBytes());
        fileService.link(userId, a.id(), OwnerType.DOCUMENT, ownerId);
        fileService.link(userId, b.id(), OwnerType.DOCUMENT, ownerId);

        List<FileResponse> files = fileService.filesForOwner(userId, OwnerType.DOCUMENT, ownerId);

        assertEquals(2, files.size());
    }
}
