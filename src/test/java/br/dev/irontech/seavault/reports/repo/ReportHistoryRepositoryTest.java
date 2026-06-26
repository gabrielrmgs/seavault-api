package br.dev.irontech.seavault.reports.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.reports.domain.ReportFormat;
import br.dev.irontech.seavault.reports.domain.ReportRecord;
import br.dev.irontech.seavault.reports.domain.ReportType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
class ReportHistoryRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ReportHistoryRepository reportHistoryRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Relatorio";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private void persistRecord(User owner, ReportType type, Instant generatedAt) {
        ReportRecord r = new ReportRecord();
        r.userId = owner.id;
        r.type = type;
        r.format = ReportFormat.JSON;
        r.params = "includeSensitive=false;sections=";
        r.generatedAt = generatedAt;
        reportHistoryRepository.persist(r);
    }

    @Test
    @Transactional
    void listByUserIsScopedAndOrderedDesc() {
        User a = persistUser("rep-a@example.com");
        User b = persistUser("rep-b@example.com");
        persistRecord(a, ReportType.DOCUMENTS, Instant.parse("2026-01-01T00:00:00Z"));
        persistRecord(a, ReportType.CERTIFICATES, Instant.parse("2026-02-01T00:00:00Z"));
        persistRecord(b, ReportType.CV, Instant.parse("2026-03-01T00:00:00Z"));

        var aRows = reportHistoryRepository.listByUser(a.id);
        assertEquals(2, aRows.size());
        assertEquals(ReportType.CERTIFICATES, aRows.get(0).type); // mais recente primeiro
        assertEquals(2L, reportHistoryRepository.countByUser(a.id));
        assertFalse(aRows.stream().anyMatch(r -> r.userId.equals(b.id)));
    }
}
