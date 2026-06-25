package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.dto.RegisterResponse;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.ConflictException;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class AuthServiceRegisterTest {

    @Inject AuthService authService;
    @Inject UserRepository userRepository;
    @Inject MockMailbox mailbox;

    @BeforeEach
    void clear() { mailbox.clear(); }

    @Test
    @Transactional
    void registersUserUnverifiedAndSendsConfirmEmail() {
        RegisterResponse resp = authService.register(
                new RegisterRequest("João", "novo@example.com", "senha1234", true));

        var user = userRepository.findActiveByEmail("novo@example.com").orElseThrow();
        assertEquals(resp.id(), user.id);
        assertFalse(user.emailVerified);
        assertEquals(1, mailbox.getMessagesSentTo("novo@example.com").size());
    }

    @Test
    @Transactional
    void rejectsDuplicateEmail() {
        authService.register(new RegisterRequest("A", "dup@example.com", "senha1234", true));
        assertThrows(ConflictException.class, () ->
                authService.register(new RegisterRequest("B", "dup@example.com", "senha1234", true)));
    }
}
