package br.dev.irontech.seavault.documents.repo;

import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.documents.domain.Document;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DocumentRepository implements PanacheRepositoryBase<Document, UUID> {

    public Optional<Document> findActiveByIdAndUser(UUID id, UUID userId) {
        return find("id = ?1 and userId = ?2 and deletedAt is null", id, userId).firstResultOptional();
    }

    public List<Document> listActiveByUser(UUID userId, PageRequest page) {
        return find("userId = ?1 and deletedAt is null", Sort.by("createdAt").descending(), userId)
                .page(page.page(), page.size())
                .list();
    }

    public long countActiveByUser(UUID userId) {
        return count("userId = ?1 and deletedAt is null", userId);
    }

    public List<Document> listExpiringAllUsers(LocalDate maxDate) {
        return find("expiryDate is not null and expiryDate <= ?1 and deletedAt is null", maxDate).list();
    }

    public List<Document> listAllActiveByUser(UUID userId) {
        return find("userId = ?1 and deletedAt is null", Sort.by("createdAt").descending(), userId).list();
    }

    public void softDeleteByUser(UUID userId, java.time.Instant deletedAt) {
        update("deletedAt = ?1 where userId = ?2 and deletedAt is null", deletedAt, userId);
    }
}
