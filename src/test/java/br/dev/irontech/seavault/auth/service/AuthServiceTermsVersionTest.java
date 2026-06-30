package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class AuthServiceTermsVersionTest {

    @Inject
    AuthService authService;

    @Inject
    UserRepository userRepository;

    @Test
    void storesAcceptedTermsVersionAtRegistration() {
        var resp = authService.register(
                new RegisterRequest("Terms", "terms-version@example.com", "senha12345", true));

        assertEquals("2026-06-29", userRepository.findById(resp.id()).termsVersion);
    }
}
