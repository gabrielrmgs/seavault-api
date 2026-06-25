package br.dev.irontech.seavault.vessels;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class VesselsSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void vesselsTableExistsAndIsEmpty() {
        Object count = em.createNativeQuery("SELECT count(*) FROM vessels").getSingleResult();
        assertEquals(0L, ((Number) count).longValue());
    }
}
