package br.dev.irontech.seavault.reports;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ReportsSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void reportHistoryTableExists() {
        Object count = em.createNativeQuery("SELECT count(*) FROM report_history").getSingleResult();
        assertTrue(((Number) count).longValue() >= 0L);
    }
}
