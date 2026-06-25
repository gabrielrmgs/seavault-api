package br.dev.irontech.seavault.files;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class FilesSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void filesTablesExist() {
        Object files = em.createNativeQuery("SELECT count(*) FROM files").getSingleResult();
        Object links = em.createNativeQuery("SELECT count(*) FROM file_links").getSingleResult();
        // Just verify tables exist (migration ran); count may be non-zero in shared test database
        assertTrue(((Number) files).longValue() >= 0);
        assertTrue(((Number) links).longValue() >= 0);
    }
}
