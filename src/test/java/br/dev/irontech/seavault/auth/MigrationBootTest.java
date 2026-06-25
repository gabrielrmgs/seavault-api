package br.dev.irontech.seavault.auth;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class MigrationBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void usersTableExistsAndIsEmpty() {
        Object count = em.createNativeQuery("SELECT count(*) FROM users").getSingleResult();
        assertEquals(0L, ((Number) count).longValue());
    }
}
