package br.dev.irontech.seavault.companies.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.companies.domain.Company;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CompanyRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    CompanyRepository companyRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Company";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private Company persistCompany(User owner) {
        Company c = new Company();
        c.userId = owner.id;
        c.name = "Empresa Teste";
        companyRepository.persist(c);
        return c;
    }

    @Test
    @Transactional
    void findActiveByIdAndUserRespectsOwnership() {
        User a = persistUser("company-a@example.com");
        User b = persistUser("company-b@example.com");
        Company c = persistCompany(a);

        assertTrue(companyRepository.findActiveByIdAndUser(c.id, a.id).isPresent());
        assertFalse(companyRepository.findActiveByIdAndUser(c.id, b.id).isPresent());
    }

    @Test
    @Transactional
    void ignoresSoftDeleted() {
        User a = persistUser("company-del@example.com");
        Company c = persistCompany(a);
        c.deletedAt = Instant.now();
        companyRepository.persist(c);

        assertFalse(companyRepository.findActiveByIdAndUser(c.id, a.id).isPresent());
    }

    @Test
    @Transactional
    void listAndCountScopedToUser() {
        User a = persistUser("company-list@example.com");
        persistCompany(a);
        persistCompany(a);

        assertEquals(2L, companyRepository.countActiveByUser(a.id));
        assertEquals(2, companyRepository.listActiveByUser(a.id, PageRequest.of(0, 20)).size());
    }
}
