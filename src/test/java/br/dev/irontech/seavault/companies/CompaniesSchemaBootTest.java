package br.dev.irontech.seavault.companies;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CompaniesSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void companiesTableExists() {
        Object count = em.createNativeQuery("SELECT count(*) FROM companies").getSingleResult();
        assertTrue(((Number) count).longValue() >= 0L);
    }
}
