package br.dev.irontech.seavault.reports.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.certificates.dto.CertificateRequest;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.documents.dto.DocumentRequest;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.reports.domain.ReportFormat;
import br.dev.irontech.seavault.reports.domain.ReportType;
import br.dev.irontech.seavault.reports.dto.ReportOptions;
import br.dev.irontech.seavault.reports.pdf.ReportDocument;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ReportServiceTest {

    @Inject ReportService reportService;
    @Inject DocumentService documentService;
    @Inject CertificateService certificateService;
    @Inject UserRepository userRepository;
    @Inject ReferenceRepository referenceRepository;

    private static final ReportOptions DEFAULT_OPTS = new ReportOptions(false, Set.of());

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

    @Test
    void documentsReportHasTableWithRowPerDocument() {
        UUID userId = newUser("rep-svc-docs@example.com");
        documentService.create(userId, new DocumentRequest(
                documentTypeId(), "123", "DPC", LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(1), null));

        ReportDocument doc = reportService.generate(userId, ReportType.DOCUMENTS, ReportFormat.JSON, DEFAULT_OPTS);

        assertEquals("DOCUMENTS", doc.type());
        assertNotNull(doc.generatedAt());
        var section = doc.sections().get(0);
        assertEquals("Documentos", section.heading());
        assertNotNull(section.table());
        assertEquals(1, section.table().rows().size());
        assertTrue(section.table().rows().get(0).contains("123"));
    }

    @Test
    void certificatesReportHasTableWithRowPerCertificate() {
        UUID userId = newUser("rep-svc-certs@example.com");
        certificateService.create(userId, new CertificateRequest(
                "STCW Basico", "BST", "Escola", LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(2), null));

        ReportDocument doc = reportService.generate(userId, ReportType.CERTIFICATES, ReportFormat.JSON, DEFAULT_OPTS);

        assertEquals("CERTIFICATES", doc.type());
        var section = doc.sections().get(0);
        assertEquals(1, section.table().rows().size());
        assertTrue(section.table().rows().get(0).contains("STCW Basico"));
    }

    @Test
    void generateRecordsHistory() {
        UUID userId = newUser("rep-svc-hist@example.com");
        reportService.generate(userId, ReportType.DOCUMENTS, ReportFormat.PDF, DEFAULT_OPTS);
        reportService.generate(userId, ReportType.CERTIFICATES, ReportFormat.JSON, DEFAULT_OPTS);

        var history = reportService.history(userId);
        assertEquals(2, history.size());
        // mais recente primeiro
        assertEquals(ReportType.CERTIFICATES, history.get(0).type());
        assertEquals(ReportFormat.PDF, history.get(1).format());
    }
}
