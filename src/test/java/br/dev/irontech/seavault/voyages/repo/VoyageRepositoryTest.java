package br.dev.irontech.seavault.voyages.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.voyages.domain.Voyage;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class VoyageRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    VoyageRepository voyageRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Voyage";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private Voyage persistVoyage(User owner) {
        Voyage v = new Voyage();
        v.userId = owner.id;
        v.embarkDate = LocalDate.of(2024, 1, 1);
        voyageRepository.persist(v);
        return v;
    }

    @Test
    @Transactional
    void findActiveByIdAndUserRespectsOwnership() {
        User a = persistUser("voyage-a@example.com");
        User b = persistUser("voyage-b@example.com");
        Voyage v = persistVoyage(a);

        assertTrue(voyageRepository.findActiveByIdAndUser(v.id, a.id).isPresent());
        assertFalse(voyageRepository.findActiveByIdAndUser(v.id, b.id).isPresent());
    }

    @Test
    @Transactional
    void ignoresSoftDeleted() {
        User a = persistUser("voyage-del@example.com");
        Voyage v = persistVoyage(a);
        v.deletedAt = Instant.now();
        voyageRepository.persist(v);

        assertFalse(voyageRepository.findActiveByIdAndUser(v.id, a.id).isPresent());
    }

    @Test
    @Transactional
    void listCountAndListAllScopedToUser() {
        User a = persistUser("voyage-list@example.com");
        persistVoyage(a);
        persistVoyage(a);

        assertEquals(2L, voyageRepository.countActiveByUser(a.id));
        assertEquals(2, voyageRepository.listActiveByUser(a.id, PageRequest.of(0, 20)).size());
        assertEquals(2, voyageRepository.listAllActiveByUser(a.id).size());
    }
}
