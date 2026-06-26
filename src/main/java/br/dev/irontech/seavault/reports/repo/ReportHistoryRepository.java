package br.dev.irontech.seavault.reports.repo;

import br.dev.irontech.seavault.reports.domain.ReportRecord;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ReportHistoryRepository implements PanacheRepositoryBase<ReportRecord, UUID> {

    public List<ReportRecord> listByUser(UUID userId) {
        return find("userId = ?1", Sort.by("generatedAt").descending(), userId).list();
    }

    public long countByUser(UUID userId) {
        return count("userId = ?1", userId);
    }
}
