package br.dev.irontech.seavault.certificates.repo;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.certificates.domain.Certificate;
import br.dev.irontech.seavault.common.page.PageRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CertificateRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    CertificateRepository certificateRepository;

    private User persistUser(String email) {
        User u = new User();
        u.name = "Dono Cert";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u;
    }

    private Certificate persistCert(User owner, String name) {
        Certificate c = new Certificate();
        c.userId = owner.id;
        c.name = name;
        certificateRepository.persist(c);
        return c;
    }

    @Test
    @Transactional
    void findActiveByIdAndUserRespectsOwnership() {
        User a = persistUser("cert-a@example.com");
        User b = persistUser("cert-b@example.com");
        Certificate c = persistCert(a, "STCW");

        assertTrue(certificateRepository.findActiveByIdAndUser(c.id, a.id).isPresent());
        assertFalse(certificateRepository.findActiveByIdAndUser(c.id, b.id).isPresent());
    }

    @Test
    @Transactional
    void ignoresSoftDeleted() {
        User a = persistUser("cert-del@example.com");
        Certificate c = persistCert(a, "ASO");
        c.deletedAt = Instant.now();
        certificateRepository.persist(c);

        assertFalse(certificateRepository.findActiveByIdAndUser(c.id, a.id).isPresent());
    }

    @Test
    @Transactional
    void listAndCountScopedToUser() {
        User a = persistUser("cert-list@example.com");
        persistCert(a, "C1");
        persistCert(a, "C2");

        assertEquals(2L, certificateRepository.countActiveByUser(a.id));
        assertEquals(2, certificateRepository.listActiveByUser(a.id, PageRequest.of(0, 20)).size());
    }
}
