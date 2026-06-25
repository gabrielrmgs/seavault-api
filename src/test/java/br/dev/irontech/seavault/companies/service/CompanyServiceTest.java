package br.dev.irontech.seavault.companies.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.companies.dto.CompanyRequest;
import br.dev.irontech.seavault.companies.dto.CompanyResponse;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class CompanyServiceTest {

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

    private UUID companyTypeId() {
        return referenceRepository.listTypes("COMPANY").get(0).id;
    }

    private UUID vesselTypeId() {
        return referenceRepository.listTypes("VESSEL").get(0).id;
    }

    private CompanyRequest req(String name, UUID typeId) {
        return new CompanyRequest(name, typeId, "12.345.678/0001-99", "rh@empresa.com", "+55 21 99999-0000", "obs");
    }

    @Test
    void createWithoutTypePersists() {
        UUID userId = newUser("company-svc-notype@example.com");
        CompanyResponse resp = companyService.create(userId, req("Empresa Sem Tipo", null));

        assertNotNull(resp.id());
        assertEquals("Empresa Sem Tipo", resp.name());
    }

    @Test
    void createWithValidTypePersists() {
        UUID userId = newUser("company-svc-type@example.com");
        UUID typeId = companyTypeId();

        CompanyResponse resp = companyService.create(userId, req("Armadora X", typeId));
        assertEquals(typeId, resp.typeId());
        assertEquals("rh@empresa.com", resp.email());
    }

    @Test
    void createWithUnknownTypeReturns404() {
        UUID userId = newUser("company-svc-badtype@example.com");
        UUID unknown = UUID.fromString("00000000-0000-0000-0000-000000000000");
        assertThrows(NotFoundException.class, () -> companyService.create(userId, req("X", unknown)));
    }

    @Test
    void createWithWrongKindTypeReturns404() {
        UUID userId = newUser("company-svc-wrongkind@example.com");
        UUID vesselType = vesselTypeId();
        assertThrows(NotFoundException.class, () -> companyService.create(userId, req("X", vesselType)));
    }

    @Test
    void updateReplacesFields() {
        UUID userId = newUser("company-svc-update@example.com");
        CompanyResponse created = companyService.create(userId, req("Old", null));

        CompanyResponse updated = companyService.update(userId, created.id(),
                new CompanyRequest("New", null, null, null, null, null));

        assertEquals("New", updated.name());
        assertEquals(null, updated.cnpj());
    }

    @Test
    void getOfAnotherUsersCompanyReturns404() {
        UUID a = newUser("company-svc-iso-a@example.com");
        UUID b = newUser("company-svc-iso-b@example.com");
        CompanyResponse c = companyService.create(a, req("Secret", null));

        assertThrows(NotFoundException.class, () -> companyService.get(b, c.id()));
    }

    @Test
    void deleteMakesItUnreadable() {
        UUID userId = newUser("company-svc-delete@example.com");
        CompanyResponse c = companyService.create(userId, req("Tmp", null));

        companyService.delete(userId, c.id());

        assertThrows(NotFoundException.class, () -> companyService.get(userId, c.id()));
        assertEquals(0, companyService.list(userId, PageRequest.of(0, 20)).content().size());
    }
}
