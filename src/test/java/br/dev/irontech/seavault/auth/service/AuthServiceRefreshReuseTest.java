package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.dto.LoginRequest;
import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.dto.TokenResponse;
import br.dev.irontech.seavault.common.error.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class AuthServiceRefreshReuseTest {

    @Inject
    AuthService authService;

    @Test
    void reusingRotatedRefreshTokenRevokesTokenFamily() {
        authService.register(new RegisterRequest("Reuse", "reuse-family@example.com", "senha12345", true));

        TokenResponse first = authService.login(new LoginRequest("reuse-family@example.com", "senha12345"));
        TokenResponse second = authService.refresh(first.refreshToken());

        assertThrows(UnauthorizedException.class, () -> authService.refresh(first.refreshToken()));
        assertThrows(UnauthorizedException.class, () -> authService.refresh(second.refreshToken()));
    }
}
