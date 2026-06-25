package br.dev.irontech.seavault.files.repo;

import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.files.domain.StoredFile;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FileRepository implements PanacheRepositoryBase<StoredFile, UUID> {

    public Optional<StoredFile> findActiveByIdAndUser(UUID id, UUID userId) {
        return find("id = ?1 and userId = ?2 and deletedAt is null", id, userId).firstResultOptional();
    }

    public List<StoredFile> listActiveByUser(UUID userId, PageRequest page) {
        return find("userId = ?1 and deletedAt is null", Sort.by("createdAt").descending(), userId)
                .page(page.page(), page.size())
                .list();
    }

    public long countActiveByUser(UUID userId) {
        return count("userId = ?1 and deletedAt is null", userId);
    }
}
