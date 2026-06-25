package br.dev.irontech.seavault.voyages.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.companies.dto.CompanyRequest;
import br.dev.irontech.seavault.companies.service.CompanyService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.vessels.dto.VesselRequest;
import br.dev.irontech.seavault.vessels.service.VesselService;
import br.dev.irontech.seavault.voyages.dto.VoyageRequest;
import br.dev.irontech.seavault.voyages.dto.VoyageResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class VoyageServiceTest {

    @Inject
    VoyageService voyageService;

    @Inject
    VesselService vesselService;

    @Inject
    CompanyService companyService;

    @Inject
    UserRepository userRepository;

    @Inject
    ReferenceRepository referenceRepository;

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

    private UUID navigationTypeId() {
        return referenceRepository.listTypes("NAVIGATION").get(0).id;
    }

    private UUID vesselTypeId() {
        return referenceRepository.listTypes("VESSEL").get(0).id;
    }

    private UUID categoryId() {
        return referenceRepository.listAllCategories().get(0).id;
    }

    private VoyageRequest finished(LocalDate embark, LocalDate disembark) {
        return new VoyageRequest(embark, disembark, null, null, null, null,
                "Comandante", "Santos", "Rio de Janeiro", null, null, "obs");
    }

    @Test
    void createFinishedComputesInclusiveDays() {
        UUID userId = newUser("voyage-svc-days@example.com");
        VoyageResponse resp = voyageService.create(userId,
                finished(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10)));

        assertNotNull(resp.id());
        assertEquals("FINISHED", resp.status());
        assertEquals(10, resp.calculatedDays());
        assertEquals(10, resp.effectiveDays());
    }

    @Test
    void createActiveHasNullCalculatedAndActiveStatus() {
        UUID userId = newUser("voyage-svc-active@example.com");
        VoyageResponse resp = voyageService.create(userId,
                new VoyageRequest(LocalDate.of(2024, 1, 1), null, null, null, null, null,
                        "Imediato", null, null, null, null, null));

        assertEquals("ACTIVE", resp.status());
        assertNull(resp.calculatedDays());
    }

    @Test
    void disembarkBeforeEmbarkReturns422() {
        UUID userId = newUser("voyage-svc-baddate@example.com");
        assertThrows(BusinessException.class, () -> voyageService.create(userId,
                finished(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 1))));
    }

    @Test
    void overrideReplacesEffectiveDays() {
        UUID userId = newUser("voyage-svc-override@example.com");
        VoyageResponse resp = voyageService.create(userId,
                new VoyageRequest(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), null, null, null, null,
                        "Comandante", null, null, 30, "ajuste contratual", null));

        assertEquals(10, resp.calculatedDays());
        assertEquals(30, resp.computedDays());
        assertEquals(30, resp.effectiveDays());
        assertNotNull(resp.overriddenAt());
    }

    @Test
    void updateReplaceAllClearsOverrideWhenOmitted() {
        UUID userId = newUser("voyage-svc-clear@example.com");
        VoyageResponse created = voyageService.create(userId,
                new VoyageRequest(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), null, null, null, null,
                        "Comandante", null, null, 30, "ajuste", null));

        VoyageResponse updated = voyageService.update(userId, created.id(),
                finished(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10)));

        assertNull(updated.computedDays());
        assertNull(updated.overriddenAt());
        assertEquals(10, updated.effectiveDays());
    }

    @Test
    void createWithValidFksPersists() {
        UUID userId = newUser("voyage-svc-fks@example.com");
        UUID vesselId = vesselService.create(userId,
                new VesselRequest("Navio", vesselTypeId(), null, null, null, null)).id();
        UUID companyId = companyService.create(userId,
                new CompanyRequest("Armadora", null, null, null, null, null)).id();
        UUID navId = navigationTypeId();
        UUID catId = categoryId();

        VoyageResponse resp = voyageService.create(userId,
                new VoyageRequest(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10),
                        vesselId, companyId, navId, catId, "Comandante", null, null, null, null, null));

        assertEquals(vesselId, resp.vesselId());
        assertEquals(companyId, resp.companyId());
        assertEquals(navId, resp.navigationTypeId());
        assertEquals(catId, resp.categoryId());
    }

    @Test
    void createWithUnknownVesselReturns404() {
        UUID userId = newUser("voyage-svc-badvessel@example.com");
        UUID unknown = UUID.fromString("00000000-0000-0000-0000-000000000000");
        assertThrows(NotFoundException.class, () -> voyageService.create(userId,
                new VoyageRequest(LocalDate.of(2024, 1, 1), null, unknown, null, null, null,
                        null, null, null, null, null, null)));
    }

    @Test
    void createWithAnotherUsersVesselReturns404() {
        UUID a = newUser("voyage-svc-vown-a@example.com");
        UUID b = newUser("voyage-svc-vown-b@example.com");
        UUID vesselOfA = vesselService.create(a,
                new VesselRequest("NavioA", vesselTypeId(), null, null, null, null)).id();

        assertThrows(NotFoundException.class, () -> voyageService.create(b,
                new VoyageRequest(LocalDate.of(2024, 1, 1), null, vesselOfA, null, null, null,
                        null, null, null, null, null, null)));
    }

    @Test
    void createWithWrongKindNavigationTypeReturns404() {
        UUID userId = newUser("voyage-svc-wrongnav@example.com");
        UUID vesselType = vesselTypeId();
        assertThrows(NotFoundException.class, () -> voyageService.create(userId,
                new VoyageRequest(LocalDate.of(2024, 1, 1), null, null, null, vesselType, null,
                        null, null, null, null, null, null)));
    }

    @Test
    void getOfAnotherUsersVoyageReturns404() {
        UUID a = newUser("voyage-svc-iso-a@example.com");
        UUID b = newUser("voyage-svc-iso-b@example.com");
        VoyageResponse v = voyageService.create(a, finished(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5)));

        assertThrows(NotFoundException.class, () -> voyageService.get(b, v.id()));
    }

    @Test
    void deleteMakesItUnreadable() {
        UUID userId = newUser("voyage-svc-delete@example.com");
        VoyageResponse v = voyageService.create(userId, finished(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5)));

        voyageService.delete(userId, v.id());

        assertThrows(NotFoundException.class, () -> voyageService.get(userId, v.id()));
        assertEquals(0, voyageService.list(userId, PageRequest.of(0, 20)).content().size());
        assertEquals(0, voyageService.listAllForUser(userId).size());
    }
}
