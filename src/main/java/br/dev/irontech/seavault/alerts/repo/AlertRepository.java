package br.dev.irontech.seavault.alerts.repo;

import br.dev.irontech.seavault.alerts.domain.Alert;
import br.dev.irontech.seavault.alerts.domain.AlertSource;
import br.dev.irontech.seavault.alerts.domain.AlertStatus;
import br.dev.irontech.seavault.common.page.PageRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AlertRepository implements PanacheRepositoryBase<Alert, UUID> {

    public Optional<Alert> findBySource(UUID userId, AlertSource source, UUID sourceId) {
        return find("userId = ?1 and source = ?2 and sourceId = ?3", userId, source, sourceId)
                .firstResultOptional();
    }

    public Optional<Alert> findByIdAndUser(UUID id, UUID userId) {
        return find("id = ?1 and userId = ?2", id, userId).firstResultOptional();
    }

    public List<Alert> listOpenAllUsers() {
        return find("status in (?1, ?2)", AlertStatus.PENDENTE, AlertStatus.LIDO).list();
    }

    public List<Alert> listByUser(UUID userId, PageRequest page) {
        return find("userId = ?1", Sort.by("createdAt").descending(), userId)
                .page(page.page(), page.size())
                .list();
    }

    public List<Alert> listByUserAndStatus(UUID userId, AlertStatus status, PageRequest page) {
        return find("userId = ?1 and status = ?2", Sort.by("createdAt").descending(), userId, status)
                .page(page.page(), page.size())
                .list();
    }

    public long countByUser(UUID userId) {
        return count("userId = ?1", userId);
    }

    public long countByUserAndStatus(UUID userId, AlertStatus status) {
        return count("userId = ?1 and status = ?2", userId, status);
    }

    public List<Alert> listPendingByUser(UUID userId) {
        return find("userId = ?1 and status = ?2", Sort.by("dueDate"), userId, AlertStatus.PENDENTE).list();
    }
}
