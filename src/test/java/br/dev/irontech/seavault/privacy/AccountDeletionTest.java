package br.dev.irontech.seavault.privacy;

import br.dev.irontech.seavault.auth.dto.LoginRequest;
import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.service.AuthService;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.error.UnauthorizedException;
import br.dev.irontech.seavault.documents.dto.DocumentRequest;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.files.service.FileService;
import br.dev.irontech.seavault.profile.dto.ProfileUpdateRequest;
import br.dev.irontech.seavault.profile.repo.ProfileRepository;
import br.dev.irontech.seavault.profile.service.ProfileService;
import br.dev.irontech.seavault.privacy.service.AccountPrivacyService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class AccountDeletionTest {

    @Inject
    AuthService authService;

    @Inject
    AccountPrivacyService privacyService;

    @Inject
    ProfileService profileService;

    @Inject
    ProfileRepository profileRepository;

    @Inject
    DocumentService documentService;

    @Inject
    FileService fileService;

    @Inject
    ReferenceRepository referenceRepository;

    @Test
    void deletedAccountCannotLogin() {
        var resp = authService.register(
                new RegisterRequest("Delete Me", "delete-me@example.com", "senha12345", true));

        privacyService.deleteAccount(resp.id());

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("delete-me@example.com", "senha12345")));
    }

    @Test
    void deletedAccountNoLongerExposesOwnedData() {
        var resp = authService.register(
                new RegisterRequest("Delete Data", "delete-data@example.com", "senha12345", true));
        var userId = resp.id();
        var documentType = referenceRepository.listTypes("DOCUMENT").get(0).id;

        profileService.update(userId, new ProfileUpdateRequest(
                "123456", "12345678901", "MG123", "BR", "21999999999", "Contato", null, null));
        documentService.create(userId, new DocumentRequest(
                documentType, "DOC-DELETE", "Marinha", LocalDate.now(), LocalDate.now().plusDays(30), null));
        fileService.upload(userId, "delete.pdf", "application/pdf", "%PDF-1.4".getBytes());

        privacyService.deleteAccount(userId);

        assertEquals(0, profileRepository.findActiveByUserId(userId).stream().count());
        assertEquals(0, documentService.list(userId, PageRequest.of(0, 20)).content().size());
        assertEquals(0, fileService.list(userId, PageRequest.of(0, 20)).content().size());
    }
}
