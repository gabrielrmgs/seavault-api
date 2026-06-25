package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.dto.LoginRequest;
import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.dto.TokenResponse;
import br.dev.irontech.seavault.common.error.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class AuthServiceLoginTest {

    @Inject AuthService authService;

    @Test
    @Transactional
    void loginReturnsTokensThenRefreshRotates() {
        authService.register(new RegisterRequest("L", "login@example.com", "senha1234", true));

        TokenResponse first = authService.login(new LoginRequest("login@example.com", "senha1234"));
        assertNotNull(first.accessToken());
        assertNotNull(first.refreshToken());

        TokenResponse rotated = authService.refresh(first.refreshToken());
        assertNotEquals(first.refreshToken(), rotated.refreshToken());

        // refresh antigo foi revogado
        assertThrows(UnauthorizedException.class, () -> authService.refresh(first.refreshToken()));
    }

    @Test
    @Transactional
    void loginRejectsWrongPassword() {
        authService.register(new RegisterRequest("W", "wrong@example.com", "senha1234", true));
        assertThrows(UnauthorizedException.class, () ->
                authService.login(new LoginRequest("wrong@example.com", "errada999")));
    }
}
