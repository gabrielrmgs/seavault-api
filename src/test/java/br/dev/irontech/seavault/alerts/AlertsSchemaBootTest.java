package br.dev.irontech.seavault.alerts;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AlertsSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void alertsTableExists() {
        Object count = em.createNativeQuery("SELECT count(*) FROM alerts").getSingleResult();
        assertTrue(((Number) count).longValue() >= 0L);
    }
}
