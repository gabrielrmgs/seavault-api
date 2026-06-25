package br.dev.irontech.seavault.auth.repo;

import br.dev.irontech.seavault.auth.domain.EmailToken;
import br.dev.irontech.seavault.auth.domain.EmailTokenType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EmailTokenRepository implements PanacheRepositoryBase<EmailToken, UUID> {

    public Optional<EmailToken> findValidByHash(String hash, EmailTokenType type, Instant now) {
        return find("tokenHash = ?1 and type = ?2 and usedAt is null and expiresAt > ?3", hash, type, now)
                .firstResultOptional();
    }
}
