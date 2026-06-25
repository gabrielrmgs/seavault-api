package br.dev.irontech.seavault.profile;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ProfilesSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void profilesTableExistsAndIsEmpty() {
        Object count = em.createNativeQuery("SELECT count(*) FROM profiles").getSingleResult();
        assertEquals(0L, ((Number) count).longValue());
    }
}
