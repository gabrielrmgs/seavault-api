package br.dev.irontech.seavault.voyages;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class VoyagesSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void voyagesTableExists() {
        Object count = em.createNativeQuery("SELECT count(*) FROM voyages").getSingleResult();
        assertTrue(((Number) count).longValue() >= 0L);
    }
}
