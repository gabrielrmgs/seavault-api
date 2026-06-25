package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.domain.EmailTokenType;
import br.dev.irontech.seavault.auth.dto.LoginRequest;
import br.dev.irontech.seavault.auth.dto.PasswordResetConfirm;
import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.repo.EmailTokenRepository;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.security.OpaqueTokens;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AuthServiceResetTest {

    @Inject AuthService authService;
    @Inject UserRepository userRepository;
    @Inject EmailTokenRepository emailTokenRepository;

    @Test
    @Transactional
    void resetChangesPasswordWithValidToken() {
        authService.register(new RegisterRequest("R", "reset@example.com", "senhaAntiga1", true));
        authService.requestPasswordReset("reset@example.com");

        var user = userRepository.findActiveByEmail("reset@example.com").orElseThrow();
        // recupera o hash do token RESET recém-criado para reconstruir o raw é inviável;
        // então geramos um novo par determinístico: persistir um token conhecido.
        String raw = OpaqueTokens.generate();
        var token = new br.dev.irontech.seavault.auth.domain.EmailToken();
        token.userId = user.id;
        token.type = EmailTokenType.RESET;
        token.tokenHash = OpaqueTokens.sha256(raw);
        token.expiresAt = Instant.now().plusSeconds(3600);
        emailTokenRepository.persist(token);

        authService.resetPassword(new PasswordResetConfirm(raw, "senhaNova1"));

        // login com a nova senha funciona
        assertNotNull(authService.login(new LoginRequest("reset@example.com", "senhaNova1")).accessToken());
    }

    @Test
    void requestForUnknownEmailIsSilent() {
        assertDoesNotThrow(() -> authService.requestPasswordReset("naoexiste@example.com"));
    }
}
