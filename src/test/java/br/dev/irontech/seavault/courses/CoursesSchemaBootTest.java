package br.dev.irontech.seavault.courses;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CoursesSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void coursesTableExists() {
        Object count = em.createNativeQuery("SELECT count(*) FROM courses").getSingleResult();
        assertTrue(((Number) count).longValue() >= 0L);
    }
}
