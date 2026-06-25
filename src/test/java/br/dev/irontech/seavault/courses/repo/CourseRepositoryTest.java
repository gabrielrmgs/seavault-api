package br.dev.irontech.seavault.courses.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.courses.domain.Course;
import br.dev.irontech.seavault.courses.domain.CourseStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CourseRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    CourseRepository courseRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Curso";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private Course persistCourse(User owner, String name) {
        Course c = new Course();
        c.userId = owner.id;
        c.name = name;
        c.status = CourseStatus.PLANEJADO;
        courseRepository.persist(c);
        return c;
    }

    @Test
    @Transactional
    void findActiveByIdAndUserRespectsOwnership() {
        User a = persistUser("course-a@example.com");
        User b = persistUser("course-b@example.com");
        Course c = persistCourse(a, "CFAQ");

        assertTrue(courseRepository.findActiveByIdAndUser(c.id, a.id).isPresent());
        assertFalse(courseRepository.findActiveByIdAndUser(c.id, b.id).isPresent());
    }

    @Test
    @Transactional
    void ignoresSoftDeleted() {
        User a = persistUser("course-del@example.com");
        Course c = persistCourse(a, "CBSP");
        c.deletedAt = Instant.now();
        courseRepository.persist(c);

        assertFalse(courseRepository.findActiveByIdAndUser(c.id, a.id).isPresent());
    }

    @Test
    @Transactional
    void listAndCountScopedToUser() {
        User a = persistUser("course-list@example.com");
        persistCourse(a, "C1");
        persistCourse(a, "C2");

        assertEquals(2L, courseRepository.countActiveByUser(a.id));
        assertEquals(2, courseRepository.listActiveByUser(a.id, PageRequest.of(0, 20)).size());
    }
}
