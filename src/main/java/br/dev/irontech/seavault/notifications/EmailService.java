package br.dev.irontech.seavault.notifications;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailService {

    private final Mailer mailer;

    public EmailService(Mailer mailer) {
        this.mailer = mailer;
    }

    public void sendConfirmEmail(String to, String confirmUrl) {
        mailer.send(Mail.withText(to,
                "SeaVault — Confirme seu e-mail",
                "Bem-vindo ao SeaVault! Confirme seu cadastro: " + confirmUrl));
    }

    public void sendPasswordReset(String to, String resetUrl) {
        mailer.send(Mail.withText(to,
                "SeaVault — Redefinição de senha",
                "Para redefinir sua senha, acesse: " + resetUrl));
    }
}
