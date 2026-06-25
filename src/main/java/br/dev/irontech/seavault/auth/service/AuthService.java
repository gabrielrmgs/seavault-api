package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.domain.EmailToken;
import br.dev.irontech.seavault.auth.domain.EmailTokenType;
import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.dto.RegisterResponse;
import br.dev.irontech.seavault.auth.repo.EmailTokenRepository;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.ConflictException;
import br.dev.irontech.seavault.common.security.OpaqueTokens;
import br.dev.irontech.seavault.notifications.EmailService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class AuthService {

    private final UserRepository userRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final EmailService emailService;

    @ConfigProperty(name = "seavault.auth.confirm-token-ttl-hours")
    long confirmTtlHours;

    @ConfigProperty(name = "seavault.app.base-url")
    String baseUrl;

    public AuthService(UserRepository userRepository,
                       EmailTokenRepository emailTokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.emailTokenRepository = emailTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        if (userRepository.emailExists(req.email())) {
            throw new ConflictException("E-mail já cadastrado");
        }
        User user = new User();
        user.name = req.name();
        user.email = req.email();
        user.passwordHash = BcryptUtil.bcryptHash(req.password());
        user.status = UserStatus.ATIVO;
        user.plan = UserPlan.FREE;
        user.role = UserRole.USER;
        user.emailVerified = false;
        user.termsAcceptedAt = Instant.now();
        userRepository.persist(user);

        String raw = OpaqueTokens.generate();
        EmailToken token = new EmailToken();
        token.userId = user.id;
        token.type = EmailTokenType.CONFIRM;
        token.tokenHash = OpaqueTokens.sha256(raw);
        token.expiresAt = Instant.now().plus(confirmTtlHours, ChronoUnit.HOURS);
        emailTokenRepository.persist(token);

        emailService.sendConfirmEmail(user.email, baseUrl + "/api/auth/confirm-email?token=" + raw);
        return new RegisterResponse(user.id, user.email);
    }

    @Transactional
    public void confirmEmail(String rawToken) {
        EmailToken token = emailTokenRepository
                .findValidByHash(OpaqueTokens.sha256(rawToken), EmailTokenType.CONFIRM, Instant.now())
                .orElseThrow(() -> new BusinessException("Token de confirmação inválido ou expirado"));
        token.usedAt = Instant.now();
        User user = userRepository.findById(token.userId);
        user.emailVerified = true;
    }
}
