package br.dev.irontech.seavault.common.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.vertx.ext.web.RoutingContext;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Provider
public class AuthRateLimitFilter implements ContainerRequestFilter {

    private static final Set<String> PROTECTED = Set.of(
            "api/auth/login",
            "api/auth/register",
            "api/auth/request-password-reset",
            "api/auth/refresh");

    private final ConcurrentHashMap<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    @Inject
    RoutingContext routingContext;

    @ConfigProperty(name = "seavault.auth.rate-limit.max-attempts", defaultValue = "10")
    int maxAttempts;

    @ConfigProperty(name = "seavault.auth.rate-limit.window-seconds", defaultValue = "60")
    long windowSeconds;

    @ConfigProperty(name = "seavault.auth.rate-limit.trusted-proxies")
    Optional<String> trustedProxiesCsv;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String path = ctx.getUriInfo().getPath();
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        if (!"POST".equals(ctx.getMethod()) || !PROTECTED.contains(normalized)) {
            return;
        }

        long bucket = Instant.now().getEpochSecond() / windowSeconds;
        attempts.keySet().removeIf(key -> !key.endsWith(":" + bucket));
        String key = clientIp(ctx) + ":" + normalized + ":" + bucket;
        AtomicInteger counter = attempts.computeIfAbsent(key, ignored -> new AtomicInteger(0));
        if (counter.incrementAndGet() > maxAttempts) {
            ctx.abortWith(Response.status(429)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"code\":\"RATE_LIMITED\",\"message\":\"Muitas tentativas. Tente novamente em instantes.\"}")
                    .build());
        }
    }

    private String clientIp(ContainerRequestContext ctx) {
        String remote = remoteAddress();
        if (isTrustedProxy(remote)) {
            String fwd = ctx.getHeaderString("X-Forwarded-For");
            if (fwd != null && !fwd.isBlank()) {
                return fwd.split(",")[0].trim();
            }
            String real = ctx.getHeaderString("X-Real-IP");
            if (real != null && !real.isBlank()) {
                return real.trim();
            }
        }
        return remote;
    }

    private String remoteAddress() {
        if (routingContext != null
                && routingContext.request() != null
                && routingContext.request().remoteAddress() != null) {
            return routingContext.request().remoteAddress().host();
        }
        return "unknown";
    }

    private boolean isTrustedProxy(String remote) {
        if (remote == null || remote.isBlank() || trustedProxiesCsv.isEmpty() || trustedProxiesCsv.get().isBlank()) {
            return false;
        }
        return Arrays.stream(trustedProxiesCsv.get().split(","))
                .map(String::trim)
                .anyMatch(remote::equals);
    }
}
