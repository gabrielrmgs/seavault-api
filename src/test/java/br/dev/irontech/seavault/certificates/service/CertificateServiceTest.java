package br.dev.irontech.seavault.certificates.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.certificates.dto.CertificateRequest;
import br.dev.irontech.seavault.certificates.dto.CertificateResponse;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.expiry.ExpiryStatus;
import br.dev.irontech.seavault.common.page.PageRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class CertificateServiceTest {

    @Inject
    CertificateService certificateService;

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

    private CertificateRequest req(String name, LocalDate expiry) {
        return new CertificateRequest(name, "STCW-A-VI/1", "CIAGA", LocalDate.of(2020, 1, 1), expiry, "obs");
    }

    @Test
    void createPersistsAndDerivesStatus() {
        UUID userId = newUser("cert-svc-create@example.com");
        CertificateResponse resp = certificateService.create(userId, req("Basic Safety", LocalDate.of(2099, 1, 1)));

        assertNotNull(resp.id());
        assertEquals("Basic Safety", resp.name());
        assertEquals(ExpiryStatus.VALIDO, resp.status());
    }

    @Test
    void nullExpiryYieldsSemValidade() {
        UUID userId = newUser("cert-svc-noexpiry@example.com");
        assertEquals(ExpiryStatus.SEM_VALIDADE, certificateService.create(userId, req("X", null)).status());
    }

    @Test
    void updateReplacesFields() {
        UUID userId = newUser("cert-svc-update@example.com");
        CertificateResponse created = certificateService.create(userId, req("Old", null));

        CertificateResponse updated = certificateService.update(userId, created.id(),
                new CertificateRequest("New", null, null, null, null, null));

        assertEquals("New", updated.name());
    }

    @Test
    void getOfAnotherUsersCertificateReturns404() {
        UUID a = newUser("cert-svc-iso-a@example.com");
        UUID b = newUser("cert-svc-iso-b@example.com");
        CertificateResponse c = certificateService.create(a, req("Secret", null));

        assertThrows(NotFoundException.class, () -> certificateService.get(b, c.id()));
    }

    @Test
    void deleteMakesItUnreadable() {
        UUID userId = newUser("cert-svc-delete@example.com");
        CertificateResponse c = certificateService.create(userId, req("Tmp", null));

        certificateService.delete(userId, c.id());

        assertThrows(NotFoundException.class, () -> certificateService.get(userId, c.id()));
        assertEquals(0, certificateService.list(userId, PageRequest.of(0, 20)).content().size());
    }
}
