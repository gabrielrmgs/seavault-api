package br.dev.irontech.seavault.profile.repo;

import br.dev.irontech.seavault.profile.domain.Profile;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProfileRepository implements PanacheRepositoryBase<Profile, UUID> {

    public Optional<Profile> findActiveByUserId(UUID userId) {
        return find("userId = ?1 and deletedAt is null", userId).firstResultOptional();
    }

    public void softDeleteByUser(UUID userId, java.time.Instant deletedAt) {
        update("deletedAt = ?1 where userId = ?2 and deletedAt is null", deletedAt, userId);
    }
}
