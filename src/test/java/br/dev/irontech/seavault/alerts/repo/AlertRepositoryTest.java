package br.dev.irontech.seavault.alerts.repo;

import br.dev.irontech.seavault.alerts.domain.Alert;
import br.dev.irontech.seavault.alerts.domain.AlertSource;
import br.dev.irontech.seavault.alerts.domain.AlertStatus;
import br.dev.irontech.seavault.alerts.domain.AlertType;
import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.page.PageRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AlertRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    AlertRepository alertRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Alerta";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private Alert persistAlert(User owner, AlertSource source, UUID sourceId, AlertStatus status) {
        Alert a = new Alert();
        a.userId = owner.id;
        a.source = source;
        a.sourceId = sourceId;
        a.type = AlertType.DOCUMENT_EXPIRY;
        a.title = "Alerta";
        a.dueDate = LocalDate.of(2026, 1, 1);
        a.leadDays = 30;
        a.status = status;
        alertRepository.persist(a);
        return a;
    }

    @Test
    @Transactional
    void findBySourceRespectsOwnership() {
        User a = persistUser("alert-a@example.com");
        User b = persistUser("alert-b@example.com");
        UUID sourceId = UUID.randomUUID();
        persistAlert(a, AlertSource.DOCUMENT, sourceId, AlertStatus.PENDENTE);

        assertTrue(alertRepository.findBySource(a.id, AlertSource.DOCUMENT, sourceId).isPresent());
        assertFalse(alertRepository.findBySource(b.id, AlertSource.DOCUMENT, sourceId).isPresent());
    }

    @Test
    @Transactional
    void statusFiltersAndCounts() {
        User a = persistUser("alert-status@example.com");
        persistAlert(a, AlertSource.DOCUMENT, UUID.randomUUID(), AlertStatus.PENDENTE);
        persistAlert(a, AlertSource.CERTIFICATE, UUID.randomUUID(), AlertStatus.IGNORADO);

        assertEquals(2L, alertRepository.countByUser(a.id));
        assertEquals(1L, alertRepository.countByUserAndStatus(a.id, AlertStatus.PENDENTE));
        assertEquals(1, alertRepository.listByUserAndStatus(a.id, AlertStatus.PENDENTE, PageRequest.of(0, 20)).size());
        assertEquals(1, alertRepository.listPendingByUser(a.id).size());
    }

    @Test
    @Transactional
    void listOpenAllUsersExcludesResolvedAndIgnored() {
        User a = persistUser("alert-open@example.com");
        UUID openId = UUID.randomUUID();
        persistAlert(a, AlertSource.DOCUMENT, openId, AlertStatus.PENDENTE);
        persistAlert(a, AlertSource.CERTIFICATE, UUID.randomUUID(), AlertStatus.RESOLVIDO);
        persistAlert(a, AlertSource.COURSE, UUID.randomUUID(), AlertStatus.IGNORADO);

        assertTrue(alertRepository.listOpenAllUsers().stream()
                .anyMatch(x -> openId.equals(x.sourceId)));
        assertFalse(alertRepository.listOpenAllUsers().stream()
                .anyMatch(x -> x.userId.equals(a.id) && x.status == AlertStatus.RESOLVIDO));
    }
}
