package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.dto.LoginRequest;
import br.dev.irontech.seavault.common.error.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthServiceTimingTest {

    @Inject
    AuthService authService;

    @Test
    void unknownEmailStillRunsBcryptComparison() {
        long start = System.nanoTime();

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("missing-timing@example.com", "qualquer-senha")));

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMs > 20,
                "login de email inexistente deve executar comparacao bcrypt; gastou " + elapsedMs + "ms");
    }
}
