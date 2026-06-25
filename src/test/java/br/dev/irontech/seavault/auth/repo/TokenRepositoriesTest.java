package br.dev.irontech.seavault.auth.repo;

import br.dev.irontech.seavault.auth.domain.EmailToken;
import br.dev.irontech.seavault.auth.domain.EmailTokenType;
import br.dev.irontech.seavault.auth.domain.RefreshToken;
import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class TokenRepositoriesTest {

    @Inject RefreshTokenRepository refreshRepo;
    @Inject EmailTokenRepository emailRepo;
    @Inject UserRepository userRepo;

    private User createUser(String email) {
        User u = new User();
        u.name = "Test User";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepo.persist(u);
        return u;
    }

    @Test
    @Transactional
    void findsValidRefreshTokenByHash() {
        User user = createUser("rt-test@example.com");

        RefreshToken rt = new RefreshToken();
        rt.userId = user.id;
        rt.tokenHash = "hash-rt";
        rt.expiresAt = Instant.now().plus(1, ChronoUnit.DAYS);
        refreshRepo.persist(rt);

        assertTrue(refreshRepo.findValidByHash("hash-rt", Instant.now()).isPresent());
    }

    @Test
    @Transactional
    void findsValidEmailTokenByHashAndType() {
        User user = createUser("et-test@example.com");

        EmailToken et = new EmailToken();
        et.userId = user.id;
        et.type = EmailTokenType.CONFIRM;
        et.tokenHash = "hash-et";
        et.expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
        emailRepo.persist(et);

        assertTrue(emailRepo.findValidByHash("hash-et", EmailTokenType.CONFIRM, Instant.now()).isPresent());
    }
}
