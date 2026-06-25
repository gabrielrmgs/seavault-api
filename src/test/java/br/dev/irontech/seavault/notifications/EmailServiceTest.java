package br.dev.irontech.seavault.notifications;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EmailServiceTest {

    @Inject EmailService emailService;
    @Inject MockMailbox mailbox;

    @BeforeEach
    void clear() {
        mailbox.clear();
    }

    @Test
    void sendsConfirmEmailToRecipient() {
        emailService.sendConfirmEmail("joao@example.com", "http://localhost:8080/confirm?token=abc");

        var sent = mailbox.getMessagesSentTo("joao@example.com");
        assertEquals(1, sent.size());
        assertTrue(sent.get(0).getText().contains("token=abc"));
    }
}
