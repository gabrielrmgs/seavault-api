package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.dto.RegisterResponse;
import br.dev.irontech.seavault.notifications.EmailService;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AuthServiceRegisterEmailFailureTest {

    @Inject
    AuthService authService;

    @Test
    void registerSucceedsEvenIfEmailFails() {
        EmailService failing = Mockito.mock(EmailService.class);
        Mockito.doThrow(new RuntimeException("smtp down"))
                .when(failing).sendConfirmEmail(Mockito.anyString(), Mockito.anyString());
        QuarkusMock.installMockForType(failing, EmailService.class);

        RegisterResponse resp = authService.register(
                new RegisterRequest("Falha Email", "falha-email@example.com", "senha12345", true));

        assertNotNull(resp.id());
    }
}
