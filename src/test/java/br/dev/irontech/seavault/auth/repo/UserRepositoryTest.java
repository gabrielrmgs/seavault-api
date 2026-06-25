package br.dev.irontech.seavault.auth.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class UserRepositoryTest {

    @Inject
    UserRepository repository;

    @Test
    @Transactional
    void persistsAndFindsActiveByEmail() {
        User u = new User();
        u.name = "João Marinheiro";
        u.email = "joao@example.com";
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        repository.persist(u);

        assertTrue(repository.findActiveByEmail("joao@example.com").isPresent());
        assertTrue(repository.emailExists("joao@example.com"));
        assertFalse(repository.findActiveByEmail("naoexiste@example.com").isPresent());
    }
}
