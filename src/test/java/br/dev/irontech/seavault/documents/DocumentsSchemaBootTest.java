package br.dev.irontech.seavault.documents;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class DocumentsSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void documentsTableExists() {
        Object count = em.createNativeQuery("SELECT count(*) FROM documents").getSingleResult();
        assertTrue(((Number) count).longValue() >= 0L);
    }
}
