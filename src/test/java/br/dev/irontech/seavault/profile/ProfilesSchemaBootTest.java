package br.dev.irontech.seavault.profile;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ProfilesSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void profilesTableExists() {
        Object count = em.createNativeQuery("SELECT count(*) FROM profiles").getSingleResult();
        assertTrue(((Number) count).longValue() >= 0L);
    }
}
