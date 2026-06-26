package br.dev.irontech.seavault.certificates;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CertificatesSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void certificatesTableExists() {
        Object count = em.createNativeQuery("SELECT count(*) FROM certificates").getSingleResult();
        assertTrue(((Number) count).longValue() >= 0L);
    }
}
