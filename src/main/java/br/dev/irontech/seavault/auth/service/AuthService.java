package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.domain.EmailToken;
import br.dev.irontech.seavault.auth.domain.EmailTokenType;
import br.dev.irontech.seavault.auth.domain.RefreshToken;
import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.dto.LoginRequest;
import br.dev.irontech.seavault.auth.dto.PasswordResetConfirm;
import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.dto.RegisterResponse;
import br.dev.irontech.seavault.auth.dto.TokenResponse;
import br.dev.irontech.seavault.auth.repo.EmailTokenRepository;
import br.dev.irontech.seavault.auth.repo.RefreshTokenRepository;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.ConflictException;
import br.dev.irontech.seavault.common.error.UnauthorizedException;
import br.dev.irontech.seavault.common.security.OpaqueTokens;
import br.dev.irontech.seavault.notifications.EmailService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    private final UserRepository userRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;

    @ConfigProperty(name = "seavault.auth.confirm-token-ttl-hours")
    long confirmTtlHours;

    @ConfigProperty(name = "seavault.auth.reset-token-ttl-hours")
    long resetTtlHours;

    @ConfigProperty(name = "seavault.app.base-url")
    String baseUrl;

    @ConfigProperty(name = "seavault.jwt.refresh-ttl-days")
    long refreshTtlDays;

    public AuthService(UserRepository userRepository,
                       EmailTokenRepository emailTokenRepository,
                       EmailService emailService,
                       RefreshTokenRepository refreshTokenRepository,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.emailTokenRepository = emailTokenRepository;
        this.emailService = emailService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
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

    @Transactional
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findActiveByEmail(req.email())
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));
        if (user.status != UserStatus.ATIVO
                || !BcryptUtil.matches(req.password(), user.passwordHash)) {
            throw new UnauthorizedException("Credenciais inválidas");
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        RefreshToken current = refreshTokenRepository
                .findValidByHash(OpaqueTokens.sha256(rawRefreshToken), Instant.now())
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));
        current.revokedAt = Instant.now();
        User user = userRepository.findById(current.userId);
        if (user == null || user.deletedAt != null || user.status != UserStatus.ATIVO) {
            throw new UnauthorizedException("Usuário indisponível");
        }
        return issueTokens(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        Optional<RefreshToken> token =
                refreshTokenRepository.findValidByHash(OpaqueTokens.sha256(rawRefreshToken), Instant.now());
        token.ifPresent(t -> t.revokedAt = Instant.now());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> maybeUser = userRepository.findActiveByEmail(email);
        if (maybeUser.isEmpty()) {
            return; // silencioso: não revela existência do e-mail
        }
        User user = maybeUser.get();
        String raw = OpaqueTokens.generate();
        EmailToken token = new EmailToken();
        token.userId = user.id;
        token.type = EmailTokenType.RESET;
        token.tokenHash = OpaqueTokens.sha256(raw);
        token.expiresAt = Instant.now().plus(resetTtlHours, ChronoUnit.HOURS);
        emailTokenRepository.persist(token);
        emailService.sendPasswordReset(user.email, baseUrl + "/api/auth/reset-password?token=" + raw);
    }

    @Transactional
    public void resetPassword(PasswordResetConfirm req) {
        EmailToken token = emailTokenRepository
                .findValidByHash(OpaqueTokens.sha256(req.token()), EmailTokenType.RESET, Instant.now())
                .orElseThrow(() -> new BusinessException("Token de redefinição inválido ou expirado"));
        token.usedAt = Instant.now();
        User user = userRepository.findById(token.userId);
        user.passwordHash = BcryptUtil.bcryptHash(req.newPassword());
        refreshTokenRepository.update("revokedAt = ?1 where userId = ?2 and revokedAt is null",
                Instant.now(), user.id);
    }

    private TokenResponse issueTokens(User user) {
        String access = tokenService.issueAccessToken(user);
        String rawRefresh = OpaqueTokens.generate();
        RefreshToken rt = new RefreshToken();
        rt.userId = user.id;
        rt.tokenHash = OpaqueTokens.sha256(rawRefresh);
        rt.expiresAt = Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS);
        refreshTokenRepository.persist(rt);
        return new TokenResponse(access, rawRefresh, tokenService.accessTtlSeconds());
    }
}
