package br.dev.irontech.seavault.dashboard.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.courses.domain.CourseStatus;
import br.dev.irontech.seavault.courses.dto.CourseRequest;
import br.dev.irontech.seavault.courses.service.CourseService;
import br.dev.irontech.seavault.dashboard.dto.DashboardResponse;
import br.dev.irontech.seavault.documents.dto.DocumentRequest;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class DashboardServiceTest {

    @Inject DashboardService dashboardService;
    @Inject DocumentService documentService;
    @Inject CourseService courseService;
    @Inject UserRepository userRepository;
    @Inject ReferenceRepository referenceRepository;

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
    void summaryBucketsDocumentsAndCourses() {
        UUID userId = newUser("dash-svc@example.com");
        UUID typeId = documentTypeId();
        // VALIDO (vence longe), VENCENDO (<=30d), VENCIDO (passado)
        documentService.create(userId, new DocumentRequest(typeId, "v", "DPC", null, LocalDate.now().plusYears(2), null));
        documentService.create(userId, new DocumentRequest(typeId, "s", "DPC", null, LocalDate.now().plusDays(10), null));
        documentService.create(userId, new DocumentRequest(typeId, "x", "DPC", null, LocalDate.now().minusDays(5), null));
        // cursos: 1 concluido, 1 planejado
        courseService.create(userId, new CourseRequest("A", null, "Esc", "EAD", 20, null, LocalDate.now(), CourseStatus.CONCLUIDO, null));
        courseService.create(userId, new CourseRequest("B", null, "Esc", "EAD", 20, LocalDate.now().plusDays(20), null, CourseStatus.PLANEJADO, null));

        DashboardResponse resp = dashboardService.summary(userId);

        assertEquals(3L, resp.documents().total());
        assertEquals(1L, resp.documents().valid());
        assertEquals(1L, resp.documents().expiring());
        assertEquals(1L, resp.documents().expired());
        assertEquals(1L, resp.courses().completed());
        assertEquals(1L, resp.courses().pending());
        assertEquals(0, resp.upcomingAlerts().size());
    }
}
