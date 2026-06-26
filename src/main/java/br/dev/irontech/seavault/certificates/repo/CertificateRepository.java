package br.dev.irontech.seavault.certificates.repo;

import br.dev.irontech.seavault.certificates.domain.Certificate;
import br.dev.irontech.seavault.common.page.PageRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CertificateRepository implements PanacheRepositoryBase<Certificate, UUID> {

    public Optional<Certificate> findActiveByIdAndUser(UUID id, UUID userId) {
        return find("id = ?1 and userId = ?2 and deletedAt is null", id, userId).firstResultOptional();
    }

    public List<Certificate> listActiveByUser(UUID userId, PageRequest page) {
        return find("userId = ?1 and deletedAt is null", Sort.by("createdAt").descending(), userId)
                .page(page.page(), page.size())
                .list();
    }

    public long countActiveByUser(UUID userId) {
        return count("userId = ?1 and deletedAt is null", userId);
    }

    public List<Certificate> listExpiringAllUsers(LocalDate maxDate) {
        return find("expiryDate is not null and expiryDate <= ?1 and deletedAt is null", maxDate).list();
    }

    public List<Certificate> listAllActiveByUser(UUID userId) {
        return find("userId = ?1 and deletedAt is null", Sort.by("createdAt").descending(), userId).list();
    }
}
