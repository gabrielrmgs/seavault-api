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
import br.dev.irontech.seavault.voyages.service.VoyageService;
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
    @jakarta.inject.Inject br.dev.irontech.seavault.voyages.service.VoyageService voyageService;
    @jakarta.inject.Inject br.dev.irontech.seavault.courses.service.CourseService courseService;

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
    void seatimeReportHasTotalsSection() {
        UUID userId = newUser("rep-svc-seatime@example.com");
        voyageService.create(userId, new br.dev.irontech.seavault.voyages.dto.VoyageRequest(
                LocalDate.now().minusDays(40), LocalDate.now().minusDays(10),
                null, null, null, null, "Comandante", null, null, null, null, null));

        ReportDocument doc = reportService.generate(userId, ReportType.SEATIME, ReportFormat.JSON, DEFAULT_OPTS);

        assertEquals("SEATIME", doc.type());
        assertTrue(doc.sections().stream().anyMatch(s -> "Totais".equals(s.heading())));
        var totals = doc.sections().stream().filter(s -> "Totais".equals(s.heading())).findFirst().orElseThrow();
        assertTrue(totals.fields().stream().anyMatch(f -> f.label().equals("Total de dias")));
    }

    @Test
    void seatimeReportRespectsSectionFilter() {
        UUID userId = newUser("rep-svc-seatime-filter@example.com");
        voyageService.create(userId, new br.dev.irontech.seavault.voyages.dto.VoyageRequest(
                LocalDate.now().minusDays(40), LocalDate.now().minusDays(10),
                null, null, null, null, "Comandante", null, null, null, null, null));

        ReportDocument doc = reportService.generate(userId, ReportType.SEATIME, ReportFormat.JSON,
                new ReportOptions(false, Set.of("totals")));

        assertEquals(1, doc.sections().size());
        assertEquals("Totais", doc.sections().get(0).heading());
    }

    @Test
    void careerReportOmitsSensitiveByDefault() {
        UUID userId = newUser("rep-svc-career@example.com");

        ReportDocument doc = reportService.generate(userId, ReportType.CAREER, ReportFormat.JSON, DEFAULT_OPTS);

        assertEquals("CAREER", doc.type());
        var profile = doc.sections().stream().filter(s -> "Perfil".equals(s.heading())).findFirst().orElseThrow();
        assertTrue(profile.fields().stream().noneMatch(f -> f.label().equals("CPF")));
    }

    @Test
    void careerReportIncludesSensitiveWhenRequested() {
        UUID userId = newUser("rep-svc-career-sens@example.com");

        ReportDocument doc = reportService.generate(userId, ReportType.CAREER, ReportFormat.JSON,
                new ReportOptions(true, Set.of()));

        var profile = doc.sections().stream().filter(s -> "Perfil".equals(s.heading())).findFirst().orElseThrow();
        assertTrue(profile.fields().stream().anyMatch(f -> f.label().equals("CPF")));
    }

    @Test
    void cvReportHasProfileCertificatesCoursesAndSeatime() {
        UUID userId = newUser("rep-svc-cv@example.com");
        certificateService.create(userId, new CertificateRequest(
                "STCW Basico", "BST", "Escola", LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(2), null));
        courseService.create(userId, new br.dev.irontech.seavault.courses.dto.CourseRequest(
                "CFAQ", null, "EFOMM", "Presencial", 200, LocalDate.now().minusMonths(2),
                LocalDate.now().minusMonths(1), br.dev.irontech.seavault.courses.domain.CourseStatus.CONCLUIDO, null));

        ReportDocument doc = reportService.generate(userId, ReportType.CV, ReportFormat.JSON, DEFAULT_OPTS);

        assertEquals("CV", doc.type());
        assertTrue(doc.sections().stream().anyMatch(s -> "Perfil".equals(s.heading())));
        var certs = doc.sections().stream().filter(s -> "Certificados".equals(s.heading())).findFirst().orElseThrow();
        assertEquals(1, certs.table().rows().size());
        var courses = doc.sections().stream().filter(s -> "Cursos".equals(s.heading())).findFirst().orElseThrow();
        assertEquals(1, courses.table().rows().size());
        assertTrue(doc.sections().stream().anyMatch(s -> "Tempo de mar".equals(s.heading())));
    }

    @Test
    void cvReportSectionFilterKeepsOnlyRequested() {
        UUID userId = newUser("rep-svc-cv-filter@example.com");

        ReportDocument doc = reportService.generate(userId, ReportType.CV, ReportFormat.JSON,
                new ReportOptions(false, Set.of("profile")));

        assertEquals(1, doc.sections().size());
        assertEquals("Perfil", doc.sections().get(0).heading());
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
