package br.dev.irontech.seavault.voyages.repo;

import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.voyages.domain.Voyage;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VoyageRepository implements PanacheRepositoryBase<Voyage, UUID> {

    public Optional<Voyage> findActiveByIdAndUser(UUID id, UUID userId) {
        return find("id = ?1 and userId = ?2 and deletedAt is null", id, userId).firstResultOptional();
    }

    public List<Voyage> listActiveByUser(UUID userId, PageRequest page) {
        return find("userId = ?1 and deletedAt is null", Sort.by("embarkDate").descending(), userId)
                .page(page.page(), page.size())
                .list();
    }

    public long countActiveByUser(UUID userId) {
        return count("userId = ?1 and deletedAt is null", userId);
    }

    public List<Voyage> listAllActiveByUser(UUID userId) {
        return find("userId = ?1 and deletedAt is null", Sort.by("embarkDate").descending(), userId).list();
    }
}
