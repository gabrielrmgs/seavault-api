package br.dev.irontech.seavault.courses.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.courses.domain.CourseStatus;
import br.dev.irontech.seavault.courses.dto.CourseRequest;
import br.dev.irontech.seavault.courses.dto.CourseResponse;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class CourseServiceTest {

    @Inject
    CourseService courseService;

    @Inject
    UserRepository userRepository;

    @Inject
    ReferenceRepository referenceRepository;

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

    private CourseRequest req(String name, UUID catalogId, CourseStatus status) {
        return new CourseRequest(name, catalogId, "CIAGA", "PRESENCIAL", 40, null, null, status, "obs");
    }

    @Test
    void createDefaultsStatusToPlanejado() {
        UUID userId = newUser("course-svc-default@example.com");
        CourseResponse resp = courseService.create(userId, req("CFAQ", null, null));

        assertNotNull(resp.id());
        assertEquals(CourseStatus.PLANEJADO, resp.status());
    }

    @Test
    void createWithCatalogCourseValidates() {
        UUID userId = newUser("course-svc-catalog@example.com");
        UUID catalogId = referenceRepository.listCourses().get(0).id;

        CourseResponse resp = courseService.create(userId, req("BST", catalogId, CourseStatus.CONCLUIDO));
        assertEquals(catalogId, resp.catalogCourseId());
        assertEquals(CourseStatus.CONCLUIDO, resp.status());
    }

    @Test
    void createWithUnknownCatalogReturns404() {
        UUID userId = newUser("course-svc-badcatalog@example.com");
        UUID unknown = UUID.fromString("00000000-0000-0000-0000-000000000000");
        assertThrows(NotFoundException.class, () -> courseService.create(userId, req("X", unknown, null)));
    }

    @Test
    void updateReplacesFields() {
        UUID userId = newUser("course-svc-update@example.com");
        CourseResponse created = courseService.create(userId, req("Old", null, CourseStatus.PLANEJADO));

        CourseResponse updated = courseService.update(userId, created.id(),
                req("New", null, CourseStatus.EM_ANDAMENTO));

        assertEquals("New", updated.name());
        assertEquals(CourseStatus.EM_ANDAMENTO, updated.status());
    }

    @Test
    void getOfAnotherUsersCourseReturns404() {
        UUID a = newUser("course-svc-iso-a@example.com");
        UUID b = newUser("course-svc-iso-b@example.com");
        CourseResponse c = courseService.create(a, req("Secret", null, null));

        assertThrows(NotFoundException.class, () -> courseService.get(b, c.id()));
    }

    @Test
    void deleteMakesItUnreadable() {
        UUID userId = newUser("course-svc-delete@example.com");
        CourseResponse c = courseService.create(userId, req("Tmp", null, null));

        courseService.delete(userId, c.id());

        assertThrows(NotFoundException.class, () -> courseService.get(userId, c.id()));
        assertEquals(0, courseService.list(userId, PageRequest.of(0, 20)).content().size());
    }
}
