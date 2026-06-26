package br.dev.irontech.seavault.alerts.scan;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.certificates.dto.CertificateRequest;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.courses.domain.CourseStatus;
import br.dev.irontech.seavault.courses.dto.CourseRequest;
import br.dev.irontech.seavault.courses.service.CourseService;
import br.dev.irontech.seavault.documents.dto.DocumentRequest;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.voyages.dto.VoyageRequest;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class DueItemsTest {

    @Inject UserRepository userRepository;
    @Inject DocumentService documentService;
    @Inject CertificateService certificateService;
    @Inject CourseService courseService;
    @Inject VoyageService voyageService;
    @Inject ReferenceRepository referenceRepository;

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

    private UUID documentTypeId() {
        return referenceRepository.listTypes("DOCUMENT").get(0).id;
    }

    @Test
    void documentExpiringIsCollected() {
        UUID userId = newUser("due-doc@example.com");
        LocalDate soon = LocalDate.now().plusDays(10);
        UUID docId = documentService.create(userId,
                new DocumentRequest(documentTypeId(), "123", "DPC", LocalDate.now().minusYears(1), soon, null)).id();

        assertTrue(documentService.dueForAlerts(LocalDate.now().plusDays(90)).stream()
                .anyMatch(i -> docId.equals(i.sourceId())));
    }

    @Test
    void certificateExpiringIsCollected() {
        UUID userId = newUser("due-cert@example.com");
        LocalDate soon = LocalDate.now().plusDays(10);
        UUID certId = certificateService.create(userId,
                new CertificateRequest("STCW", "BST", "Escola", LocalDate.now().minusYears(1), soon, null)).id();

        assertTrue(certificateService.dueForAlerts(LocalDate.now().plusDays(90)).stream()
                .anyMatch(i -> certId.equals(i.sourceId())));
    }

    @Test
    void plannedCourseStartingIsCollected() {
        UUID userId = newUser("due-course@example.com");
        LocalDate soon = LocalDate.now().plusDays(10);
        UUID courseId = courseService.create(userId,
                new CourseRequest("CFAQ", null, "EFOMM", "Presencial", 200, soon, null, CourseStatus.PLANEJADO, null)).id();

        assertTrue(courseService.dueForAlerts(LocalDate.now().plusDays(90)).stream()
                .anyMatch(i -> courseId.equals(i.sourceId())));
    }

    @Test
    void longRunningActiveVoyageIsCollected() {
        UUID userId = newUser("due-voyage@example.com");
        LocalDate oldEmbark = LocalDate.now().minusDays(300);
        UUID voyageId = voyageService.create(userId,
                new VoyageRequest(oldEmbark, null, null, null, null, null, "Comandante", null, null, null, null, null)).id();

        assertTrue(voyageService.dueForAlerts(LocalDate.now().minusDays(180)).stream()
                .anyMatch(i -> voyageId.equals(i.sourceId())));
    }
}
