package br.dev.irontech.seavault.privacy;

import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.service.AuthService;
import br.dev.irontech.seavault.certificates.dto.CertificateRequest;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.companies.dto.CompanyRequest;
import br.dev.irontech.seavault.companies.service.CompanyService;
import br.dev.irontech.seavault.courses.dto.CourseRequest;
import br.dev.irontech.seavault.courses.service.CourseService;
import br.dev.irontech.seavault.documents.dto.DocumentRequest;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.profile.dto.ProfileUpdateRequest;
import br.dev.irontech.seavault.profile.service.ProfileService;
import br.dev.irontech.seavault.privacy.dto.AccountExport;
import br.dev.irontech.seavault.privacy.service.AccountPrivacyService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.vessels.dto.VesselRequest;
import br.dev.irontech.seavault.vessels.service.VesselService;
import br.dev.irontech.seavault.voyages.dto.VoyageRequest;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AccountExportTest {

    @Inject
    AuthService authService;

    @Inject
    AccountPrivacyService privacyService;

    @Inject
    ProfileService profileService;

    @Inject
    DocumentService documentService;

    @Inject
    CertificateService certificateService;

    @Inject
    CourseService courseService;

    @Inject
    VesselService vesselService;

    @Inject
    CompanyService companyService;

    @Inject
    VoyageService voyageService;

    @Inject
    ReferenceRepository referenceRepository;

    @Test
    void exportsPortableAccountData() {
        var resp = authService.register(
                new RegisterRequest("Export Me", "export-me@example.com", "senha12345", true));
        UUID userId = resp.id();

        UUID documentType = referenceRepository.listTypes("DOCUMENT").get(0).id;
        UUID vesselType = referenceRepository.listTypes("VESSEL").get(0).id;
        UUID companyType = referenceRepository.listTypes("COMPANY").get(0).id;
        UUID navigationType = referenceRepository.listTypes("NAVIGATION").get(0).id;

        profileService.update(userId, new ProfileUpdateRequest(
                "123456", "12345678901", "MG123", "BR", "21999999999", "Contato", null, null));
        documentService.create(userId, new DocumentRequest(
                documentType, "DOC-1", "Marinha", LocalDate.now(), LocalDate.now().plusDays(30), null));
        certificateService.create(userId, new CertificateRequest(
                "CBSP", "CERT-1", "Escola", LocalDate.now(), LocalDate.now().plusDays(60), null));
        courseService.create(userId, new CourseRequest(
                "Curso", null, "Escola", "Online", 8, LocalDate.now(), null, null, null));
        var vessel = vesselService.create(userId, new VesselRequest(
                "Navio", vesselType, "1234567", "BR", BigDecimal.TEN, null));
        var company = companyService.create(userId, new CompanyRequest(
                "Empresa", companyType, "12345678000199", "ops@example.com", "21999999999", null));
        voyageService.create(userId, new VoyageRequest(
                LocalDate.now().minusDays(10), LocalDate.now(), vessel.id(), company.id(),
                navigationType, null, "Marinheiro", "Rio", "Santos", null, null, null));

        AccountExport export = privacyService.exportData(userId);

        assertNotNull(export.account());
        assertEquals("export-me@example.com", export.account().email());
        assertNotNull(export.profile());
        assertEquals(1, export.documents().size());
        assertEquals(1, export.certificates().size());
        assertEquals(1, export.courses().size());
        assertEquals(1, export.vessels().size());
        assertEquals(1, export.companies().size());
        assertEquals(1, export.voyages().size());
        assertNotNull(export.exportedAt());
    }
}
