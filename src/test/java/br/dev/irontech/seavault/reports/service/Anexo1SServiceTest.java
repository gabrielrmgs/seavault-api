package br.dev.irontech.seavault.reports.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.companies.dto.CompanyRequest;
import br.dev.irontech.seavault.companies.dto.CompanyResponse;
import br.dev.irontech.seavault.companies.service.CompanyService;
import br.dev.irontech.seavault.profile.dto.ProfileUpdateRequest;
import br.dev.irontech.seavault.profile.service.ProfileService;
import br.dev.irontech.seavault.reports.domain.ReportFormat;
import br.dev.irontech.seavault.reports.dto.Anexo1SRequest;
import br.dev.irontech.seavault.reports.pdf.ReportDocument;
import br.dev.irontech.seavault.vessels.dto.VesselRequest;
import br.dev.irontech.seavault.vessels.dto.VesselResponse;
import br.dev.irontech.seavault.vessels.service.VesselService;
import br.dev.irontech.seavault.voyages.dto.VoyageRequest;
import br.dev.irontech.seavault.voyages.dto.VoyageResponse;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class Anexo1SServiceTest {

    @Inject Anexo1SService anexo1SService;
    @Inject ProfileService profileService;
    @Inject UserRepository userRepository;
    @Inject VesselService vesselService;
    @Inject CompanyService companyService;
    @Inject VoyageService voyageService;

    @Transactional
    UUID newUser(String email) {
        User u = new User();
        u.name = "Anexo User";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u.id;
    }

    private void completeProfile(UUID userId) {
        profileService.update(userId, new ProfileUpdateRequest(
                "CIR123", "12345678901", null, "Brasileira", null, null, null, null));
    }

    private VoyageResponse finishedVoyage(UUID userId) {
        VesselResponse vessel = vesselService.create(userId, new VesselRequest(
                "Navio Teste", null, null, null, null, null));
        CompanyResponse company = companyService.create(userId, new CompanyRequest(
                "Empresa Teste", null, null, null, null, null));
        return voyageService.create(userId, new VoyageRequest(
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 3, 10),
                vessel.id(),
                company.id(),
                null,
                null,
                "Comandante",
                "Santos",
                "Rio",
                null,
                null,
                null));
    }

    private VoyageResponse activeVoyage(UUID userId) {
        VesselResponse vessel = vesselService.create(userId, new VesselRequest(
                "Navio Ativo", null, null, null, null, null));
        CompanyResponse company = companyService.create(userId, new CompanyRequest(
                "Empresa Ativa", null, null, null, null, null));
        return voyageService.create(userId, new VoyageRequest(
                LocalDate.of(2025, 4, 1),
                null,
                vessel.id(),
                company.id(),
                null,
                null,
                "Imediato",
                "Santos",
                null,
                null,
                null,
                null));
    }

    @Test
    void validRequestBuildsAttestationDocument() {
        UUID userId = newUser("anexo-svc-valid@example.com");
        completeProfile(userId);
        VoyageResponse voyage = finishedVoyage(userId);

        ReportDocument doc = anexo1SService.generate(
                userId, new Anexo1SRequest(List.of(voyage.id())), ReportFormat.JSON);

        assertEquals("ANEXO_1S", doc.type());
        assertNotNull(doc.generatedAt());
        assertEquals(3, doc.sections().size());
        assertEquals("Identificação do Aquaviário", doc.sections().get(0).heading());
        assertEquals("Embarques Atestados", doc.sections().get(1).heading());
        assertEquals(1, doc.sections().get(1).table().rows().size());
        assertEquals("Resumo", doc.sections().get(2).heading());
    }

    @Test
    void incompleteProfileReturns422WithCpfAndCirFields() {
        UUID userId = newUser("anexo-svc-profile@example.com");
        VoyageResponse voyage = finishedVoyage(userId);

        BusinessException ex = assertThrows(BusinessException.class, () -> anexo1SService.generate(
                userId, new Anexo1SRequest(List.of(voyage.id())), ReportFormat.JSON));

        assertEquals(422, ex.status());
        assertTrue(ex.fieldErrors().stream().anyMatch(e -> "cpf".equals(e.field())));
        assertTrue(ex.fieldErrors().stream().anyMatch(e -> "cir".equals(e.field())));
    }

    @Test
    void emptyVoyagesReturns422WithVoyageIdsField() {
        UUID userId = newUser("anexo-svc-empty@example.com");
        completeProfile(userId);

        BusinessException ex = assertThrows(BusinessException.class, () -> anexo1SService.generate(
                userId, new Anexo1SRequest(List.of()), ReportFormat.JSON));

        assertTrue(ex.fieldErrors().stream().anyMatch(e -> "voyageIds".equals(e.field())));
    }

    @Test
    void activeVoyageReturns422WithDisembarkDateField() {
        UUID userId = newUser("anexo-svc-active@example.com");
        completeProfile(userId);
        VoyageResponse voyage = activeVoyage(userId);

        BusinessException ex = assertThrows(BusinessException.class, () -> anexo1SService.generate(
                userId, new Anexo1SRequest(List.of(voyage.id())), ReportFormat.JSON));

        assertTrue(ex.fieldErrors().stream()
                .anyMatch(e -> e.field().contains("disembarkDate")));
    }

    @Test
    void voyageFromAnotherUserReturns404BeforeCompletenessValidation() {
        UUID owner = newUser("anexo-svc-owner@example.com");
        completeProfile(owner);
        UUID otherVoyageId = finishedVoyage(owner).id();

        UUID intruder = newUser("anexo-svc-intruder@example.com");
        completeProfile(intruder);

        assertThrows(NotFoundException.class, () -> anexo1SService.generate(
                intruder, new Anexo1SRequest(List.of(otherVoyageId)), ReportFormat.JSON));
    }
}
