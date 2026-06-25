package br.dev.irontech.seavault.vessels.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.vessels.domain.Vessel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class VesselRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    VesselRepository vesselRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Vessel";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private Vessel persistVessel(User owner) {
        Vessel v = new Vessel();
        v.userId = owner.id;
        v.name = "Navio Teste";
        vesselRepository.persist(v);
        return v;
    }

    @Test
    @Transactional
    void findActiveByIdAndUserRespectsOwnership() {
        User a = persistUser("vessel-a@example.com");
        User b = persistUser("vessel-b@example.com");
        Vessel v = persistVessel(a);

        assertTrue(vesselRepository.findActiveByIdAndUser(v.id, a.id).isPresent());
        assertFalse(vesselRepository.findActiveByIdAndUser(v.id, b.id).isPresent());
    }

    @Test
    @Transactional
    void ignoresSoftDeleted() {
        User a = persistUser("vessel-del@example.com");
        Vessel v = persistVessel(a);
        v.deletedAt = Instant.now();
        vesselRepository.persist(v);

        assertFalse(vesselRepository.findActiveByIdAndUser(v.id, a.id).isPresent());
    }

    @Test
    @Transactional
    void listAndCountScopedToUser() {
        User a = persistUser("vessel-list@example.com");
        persistVessel(a);
        persistVessel(a);

        assertEquals(2L, vesselRepository.countActiveByUser(a.id));
        assertEquals(2, vesselRepository.listActiveByUser(a.id, PageRequest.of(0, 20)).size());
    }
}
