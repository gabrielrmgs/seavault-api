package br.dev.irontech.seavault.files.repo;

import br.dev.irontech.seavault.files.domain.FileLink;
import br.dev.irontech.seavault.files.domain.OwnerType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FileLinkRepository implements PanacheRepositoryBase<FileLink, UUID> {

    public Optional<FileLink> findActive(UUID fileId, OwnerType ownerType, UUID ownerId) {
        return find("fileId = ?1 and ownerType = ?2 and ownerId = ?3 and deletedAt is null",
                fileId, ownerType, ownerId).firstResultOptional();
    }

    public List<FileLink> listActiveByOwner(OwnerType ownerType, UUID ownerId) {
        return find("ownerType = ?1 and ownerId = ?2 and deletedAt is null", ownerType, ownerId).list();
    }

    public List<FileLink> listActiveByFile(UUID fileId) {
        return find("fileId = ?1 and deletedAt is null", fileId).list();
    }
}
