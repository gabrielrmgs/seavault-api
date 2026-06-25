package br.dev.irontech.seavault.vessels.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.vessels.dto.VesselRequest;
import br.dev.irontech.seavault.vessels.dto.VesselResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class VesselServiceTest {

    @Inject
    VesselService vesselService;

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

    private UUID vesselTypeId() {
        return referenceRepository.listTypes("VESSEL").get(0).id;
    }

    private UUID companyTypeId() {
        return referenceRepository.listTypes("COMPANY").get(0).id;
    }

    private VesselRequest req(String name, UUID typeId) {
        return new VesselRequest(name, typeId, "IMO 1234567", "Brasil", new BigDecimal("19.50"), "obs");
    }

    @Test
    void createWithoutTypePersists() {
        UUID userId = newUser("vessel-svc-notype@example.com");
        VesselResponse resp = vesselService.create(userId, req("Navio Sem Tipo", null));

        assertNotNull(resp.id());
        assertEquals("Navio Sem Tipo", resp.name());
    }

    @Test
    void createWithValidTypePersists() {
        UUID userId = newUser("vessel-svc-type@example.com");
        UUID typeId = vesselTypeId();

        VesselResponse resp = vesselService.create(userId, req("Cargueiro X", typeId));
        assertEquals(typeId, resp.typeId());
        assertEquals(new BigDecimal("19.50"), resp.grossTonnage());
    }

    @Test
    void createWithUnknownTypeReturns404() {
        UUID userId = newUser("vessel-svc-badtype@example.com");
        UUID unknown = UUID.fromString("00000000-0000-0000-0000-000000000000");
        assertThrows(NotFoundException.class, () -> vesselService.create(userId, req("X", unknown)));
    }

    @Test
    void createWithWrongKindTypeReturns404() {
        UUID userId = newUser("vessel-svc-wrongkind@example.com");
        UUID companyType = companyTypeId();
        assertThrows(NotFoundException.class, () -> vesselService.create(userId, req("X", companyType)));
    }

    @Test
    void updateReplacesFields() {
        UUID userId = newUser("vessel-svc-update@example.com");
        VesselResponse created = vesselService.create(userId, req("Old", null));

        VesselResponse updated = vesselService.update(userId, created.id(),
                new VesselRequest("New", null, null, null, null, null));

        assertEquals("New", updated.name());
        assertEquals(null, updated.imo());
    }

    @Test
    void getOfAnotherUsersVesselReturns404() {
        UUID a = newUser("vessel-svc-iso-a@example.com");
        UUID b = newUser("vessel-svc-iso-b@example.com");
        VesselResponse v = vesselService.create(a, req("Secret", null));

        assertThrows(NotFoundException.class, () -> vesselService.get(b, v.id()));
    }

    @Test
    void deleteMakesItUnreadable() {
        UUID userId = newUser("vessel-svc-delete@example.com");
        VesselResponse v = vesselService.create(userId, req("Tmp", null));

        vesselService.delete(userId, v.id());

        assertThrows(NotFoundException.class, () -> vesselService.get(userId, v.id()));
        assertEquals(0, vesselService.list(userId, PageRequest.of(0, 20)).content().size());
    }
}
