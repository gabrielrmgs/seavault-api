package br.dev.irontech.seavault.profile.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.profile.dto.ProfileResponse;
import br.dev.irontech.seavault.profile.dto.ProfileUpdateRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class ProfileServiceTest {

    @Inject
    ProfileService service;

    @Inject
    UserRepository userRepository;

    @Transactional
    UUID newUser(String email) {
        User u = new User();
        u.name = "Perfil Service";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u.id;
    }

    @Test
    void getOrCreateReturnsEmptyProfileFirstTime() {
        UUID userId = newUser("svc-getcreate@example.com");
        ProfileResponse resp = service.getOrCreate(userId);
        assertNotNull(resp.id());
        assertEquals(userId, resp.userId());
        assertEquals(0, resp.completionPercent());
    }

    @Test
    void updateRecalculatesCompletion() {
        UUID userId = newUser("svc-update@example.com");
        ProfileResponse resp = service.update(userId, new ProfileUpdateRequest(
                "12345", "00000000000", "MG-1", "Brasileira", "+5531999999999",
                "Contato 31988887777", null, null));
        // 6 de 7 campos core preenchidos (categoryId nulo) → round(6/7*100) = 86
        assertEquals(86, resp.completionPercent());
    }

    @Test
    void updateRejectsUnknownCategory() {
        UUID userId = newUser("svc-badcat@example.com");
        assertThrows(NotFoundException.class, () -> service.update(userId, new ProfileUpdateRequest(
                null, null, null, null, null, null, UUID.randomUUID(), null)));
    }

    @Test
    void secondGetReturnsSameProfile() {
        UUID userId = newUser("svc-idempotent@example.com");
        UUID first = service.getOrCreate(userId).id();
        UUID second = service.getOrCreate(userId).id();
        assertEquals(first, second);
    }
}
