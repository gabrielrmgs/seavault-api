package br.dev.irontech.seavault.alerts.service;

import br.dev.irontech.seavault.alerts.domain.Alert;
import br.dev.irontech.seavault.alerts.domain.AlertSource;
import br.dev.irontech.seavault.alerts.domain.AlertStatus;
import br.dev.irontech.seavault.alerts.repo.AlertRepository;
import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.documents.dto.DocumentRequest;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AlertServiceTest {

    @Inject AlertService alertService;
    @Inject AlertRepository alertRepository;
    @Inject DocumentService documentService;
    @Inject UserRepository userRepository;
    @Inject ReferenceRepository referenceRepository;
    @Inject MockMailbox mailbox;

    @BeforeEach
    void clearMailbox() {
        mailbox.clear();
    }

    @Transactional
    UUID newUser(String email) {
        User u = new User();
        u.name = "Marinheiro";
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

    private UUID createExpiringDocument(UUID userId) {
        return documentService.create(userId, new DocumentRequest(
                documentTypeId(), "123", "DPC",
                LocalDate.now().minusYears(1), LocalDate.now().plusDays(10), null)).id();
    }

    @Test
    void scanCreatesPendingAlertForExpiringDocument() {
        UUID userId = newUser("scan-create@example.com");
        UUID docId = createExpiringDocument(userId);

        alertService.runDailyScan();

        Alert a = alertRepository.findBySource(userId, AlertSource.DOCUMENT, docId).orElseThrow();
        assertEquals(AlertStatus.PENDENTE, a.status);
    }

    @Test
    void rerunIsIdempotent() {
        UUID userId = newUser("scan-idem@example.com");
        createExpiringDocument(userId);

        alertService.runDailyScan();
        alertService.runDailyScan();

        assertEquals(1L, alertRepository.countByUser(userId));
    }

    @Test
    void autoResolvesWhenEntityNoLongerQualifies() {
        UUID userId = newUser("scan-resolve@example.com");
        UUID docId = createExpiringDocument(userId);
        alertService.runDailyScan();

        documentService.delete(userId, docId); // some da varredura
        alertService.runDailyScan();

        Alert a = alertRepository.findBySource(userId, AlertSource.DOCUMENT, docId).orElseThrow();
        assertEquals(AlertStatus.RESOLVIDO, a.status);
    }

    @Test
    void preservesUserSetIgnoredStatus() {
        UUID userId = newUser("scan-ignore@example.com");
        UUID docId = createExpiringDocument(userId);
        alertService.runDailyScan();
        setStatus(userId, docId, AlertStatus.IGNORADO);

        alertService.runDailyScan();

        Alert a = alertRepository.findBySource(userId, AlertSource.DOCUMENT, docId).orElseThrow();
        assertEquals(AlertStatus.IGNORADO, a.status);
    }

    @Test
    void sendsDigestToUserWithNewAlert() {
        UUID userId = newUser("scan-digest@example.com");
        createExpiringDocument(userId);

        alertService.runDailyScan();

        assertTrue(mailbox.getMailsSentTo("scan-digest@example.com").size() >= 1);
    }

    @Transactional
    void setStatus(UUID userId, UUID docId, AlertStatus status) {
        Alert a = alertRepository.findBySource(userId, AlertSource.DOCUMENT, docId).orElseThrow();
        a.status = status;
        alertRepository.persist(a);
    }
}
