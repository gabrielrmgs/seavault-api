package br.dev.irontech.seavault.auth.repo;

import br.dev.irontech.seavault.auth.domain.RefreshToken;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RefreshTokenRepository implements PanacheRepositoryBase<RefreshToken, UUID> {

    public Optional<RefreshToken> findValidByHash(String hash, Instant now) {
        return find("tokenHash = ?1 and revokedAt is null and expiresAt > ?2", hash, now)
                .firstResultOptional();
    }
}
