package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class TokenServiceTest {

    @Inject TokenService tokenService;

    @Test
    void issuesSignedJwtWithThreeSegments() {
        User u = new User();
        u.id = UUID.randomUUID();
        u.email = "joao@example.com";
        u.role = UserRole.USER;
        u.plan = UserPlan.FREE;
        u.status = UserStatus.ATIVO;

        String token = tokenService.issueAccessToken(u);

        assertEquals(3, token.split("\\.").length);
        assertTrue(tokenService.accessTtlSeconds() > 0);
    }
}
