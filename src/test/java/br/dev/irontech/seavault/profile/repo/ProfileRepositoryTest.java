package br.dev.irontech.seavault.profile.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.profile.domain.Profile;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ProfileRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ProfileRepository profileRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Perfil";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    @Test
    @Transactional
    void persistsAndFindsActiveByUserId() {
        User user = persistUser("perfil-repo@example.com");
        Profile p = new Profile();
        p.userId = user.id;
        p.cir = "12345";
        profileRepository.persist(p);

        assertTrue(profileRepository.findActiveByUserId(user.id).isPresent());
    }

    @Test
    @Transactional
    void ignoresSoftDeletedProfile() {
        User user = persistUser("perfil-deleted@example.com");
        Profile p = new Profile();
        p.userId = user.id;
        p.deletedAt = Instant.now();
        profileRepository.persist(p);

        assertFalse(profileRepository.findActiveByUserId(user.id).isPresent());
    }
}
