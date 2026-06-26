package br.dev.irontech.seavault.reference;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EligibilitySchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void eligibilityTablesExist() {
        Object rules = em.createNativeQuery("SELECT count(*) FROM ref_eligibility_rules").getSingleResult();
        Object reqs = em.createNativeQuery("SELECT count(*) FROM ref_eligibility_requirements").getSingleResult();
        assertTrue(((Number) rules).longValue() >= 0L);
        assertTrue(((Number) reqs).longValue() >= 0L);
    }
}
