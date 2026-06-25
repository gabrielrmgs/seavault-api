package br.dev.irontech.seavault.reference.repo;

import br.dev.irontech.seavault.reference.domain.Category;
import br.dev.irontech.seavault.reference.domain.CourseCatalog;
import br.dev.irontech.seavault.reference.domain.ProfessionalGroup;
import br.dev.irontech.seavault.reference.domain.RefType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ReferenceRepository implements PanacheRepositoryBase<ProfessionalGroup, UUID> {

    public List<ProfessionalGroup> listGroups() {
        return findAll(Sort.by("displayOrder")).list();
    }

    public List<Category> listCategoriesByGroup(UUID groupId) {
        return getEntityManager()
                .createQuery("from Category where groupId = ?1 order by progressionOrder", Category.class)
                .setParameter(1, groupId)
                .getResultList();
    }

    public List<Category> listAllCategories() {
        return getEntityManager()
                .createQuery("from Category order by progressionOrder", Category.class)
                .getResultList();
    }

    public Optional<Category> findCategoryById(UUID id) {
        return Optional.ofNullable(getEntityManager().find(Category.class, id));
    }

    public Optional<RefType> findTypeById(UUID id) {
        return Optional.ofNullable(getEntityManager().find(RefType.class, id));
    }

    public Optional<CourseCatalog> findCourseById(UUID id) {
        return Optional.ofNullable(getEntityManager().find(CourseCatalog.class, id));
    }

    public List<CourseCatalog> listCourses() {
        return getEntityManager()
                .createQuery("from CourseCatalog order by name", CourseCatalog.class)
                .getResultList();
    }

    public List<RefType> listTypes(String kind) {
        return getEntityManager()
                .createQuery("from RefType where kind = ?1 order by label", RefType.class)
                .setParameter(1, kind)
                .getResultList();
    }
}
