package br.dev.irontech.seavault.seatime.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse;
import br.dev.irontech.seavault.voyages.dto.VoyageRequest;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class SeatimeServiceTest {

    @Inject
    SeatimeService seatimeService;

    @Inject
    VoyageService voyageService;

    @Inject
    UserRepository userRepository;

    @Transactional
    UUID newUser(String email) {
        User u = new User();
        u.name = "Dono";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u.id;
    }

    private VoyageRequest voyage(LocalDate embark, LocalDate disembark, String role) {
        return new VoyageRequest(embark, disembark, null, null, null, null,
                role, null, null, null, null, null);
    }

    @Test
    void emptyUserHasZeroTotals() {
        UUID userId = newUser("seatime-empty@example.com");
        SeatimeSummaryResponse s = seatimeService.summary(userId);

        assertEquals(0L, s.totalDays());
        assertEquals(0L, s.activeDays());
        assertEquals(0.0, s.averageDaysPerContract());
        assertTrue(s.byRole().isEmpty());
    }

    @Test
    void totalsAndBreakdownsOverFinishedVoyages() {
        UUID userId = newUser("seatime-totals@example.com");
        // 10 dias (inclusivo) + 5 dias (inclusivo) = 15
        voyageService.create(userId, voyage(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), "Comandante"));
        voyageService.create(userId, voyage(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5), "Comandante"));

        SeatimeSummaryResponse s = seatimeService.summary(userId);

        assertEquals(15L, s.totalDays());
        assertEquals(7.5, s.averageDaysPerContract());
        assertEquals(10, s.longestContractDays());
        assertEquals(1, s.byRole().size());
        assertEquals("Comandante", s.byRole().get(0).role());
        assertEquals(15L, s.byRole().get(0).days());
        assertEquals(2024, s.byYear().get(0).year());
    }

    @Test
    void multipleActiveVoyagesProduceWarning() {
        UUID userId = newUser("seatime-active@example.com");
        voyageService.create(userId, voyage(LocalDate.of(2024, 1, 1), null, "Imediato"));
        voyageService.create(userId, voyage(LocalDate.of(2024, 2, 1), null, "Imediato"));

        SeatimeSummaryResponse s = seatimeService.summary(userId);

        assertTrue(s.activeDays() > 0);
        assertTrue(s.warnings().stream().anyMatch(w -> w.toLowerCase().contains("ativo")));
    }

    @Test
    void scopedToUser() {
        UUID a = newUser("seatime-iso-a@example.com");
        UUID b = newUser("seatime-iso-b@example.com");
        voyageService.create(a, voyage(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), "Comandante"));

        assertEquals(0L, seatimeService.summary(b).totalDays());
    }
}
