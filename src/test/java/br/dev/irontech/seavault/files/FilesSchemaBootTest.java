package br.dev.irontech.seavault.files;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class FilesSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void filesTablesExistAndAreEmpty() {
        Object files = em.createNativeQuery("SELECT count(*) FROM files").getSingleResult();
        Object links = em.createNativeQuery("SELECT count(*) FROM file_links").getSingleResult();
        assertEquals(0L, ((Number) files).longValue());
        assertEquals(0L, ((Number) links).longValue());
    }
}
