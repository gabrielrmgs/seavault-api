package br.dev.irontech.seavault.auth.repo;

import br.dev.irontech.seavault.auth.domain.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    public Optional<User> findActiveByEmail(String email) {
        return find("email = ?1 and deletedAt is null", email).firstResultOptional();
    }

    public boolean emailExists(String email) {
        return count("email = ?1 and deletedAt is null", email) > 0;
    }
}
