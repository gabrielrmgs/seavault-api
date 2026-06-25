package br.dev.irontech.seavault.auth.service;

import br.dev.irontech.seavault.auth.domain.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "seavault.jwt.access-ttl-seconds")
    long accessTtlSeconds;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String issueAccessToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.email)
                .subject(user.id.toString())
                .groups(Set.of(user.role.name()))
                .claim("plan", user.plan.name())
                .expiresIn(Duration.ofSeconds(accessTtlSeconds))
                .sign();
    }

    public long accessTtlSeconds() {
        return accessTtlSeconds;
    }
}
