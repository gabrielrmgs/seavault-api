package br.dev.irontech.seavault.notifications;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    private final Mailer mailer;

    public EmailService(Mailer mailer) {
        this.mailer = mailer;
    }

    public void sendConfirmEmail(String to, String confirmUrl) {
        send(to, "SeaVault - Confirme seu e-mail",
                "Bem-vindo ao SeaVault! Confirme seu cadastro: " + confirmUrl);
    }

    public void sendPasswordReset(String to, String resetUrl) {
        send(to, "SeaVault - Redefinicao de senha",
                "Para redefinir sua senha, acesse: " + resetUrl);
    }

    public void sendAlertDigest(String to, String summary) {
        send(to, "SeaVault - Seus alertas", summary);
    }

    private void send(String to, String subject, String body) {
        try {
            mailer.send(Mail.withText(to, subject, body));
        } catch (RuntimeException e) {
            LOG.errorf(e, "Falha ao enviar e-mail '%s' para %s", subject, to);
        }
    }
}
