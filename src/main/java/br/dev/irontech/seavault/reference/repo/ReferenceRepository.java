package br.dev.irontech.seavault.reference.repo;

import br.dev.irontech.seavault.reference.domain.Category;
import br.dev.irontech.seavault.reference.domain.CourseCatalog;
import br.dev.irontech.seavault.reference.domain.EligibilityRequirement;
import br.dev.irontech.seavault.reference.domain.EligibilityRule;
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

    public Optional<EligibilityRule> findRuleByTargetCategory(UUID categoryId) {
        return getEntityManager()
                .createQuery("from EligibilityRule where targetCategoryId = ?1", EligibilityRule.class)
                .setParameter(1, categoryId)
                .getResultStream()
                .findFirst();
    }

    public Optional<EligibilityRule> findRuleByTargetCourse(UUID courseId) {
        return getEntityManager()
                .createQuery("from EligibilityRule where targetCourseId = ?1", EligibilityRule.class)
                .setParameter(1, courseId)
                .getResultStream()
                .findFirst();
    }

    public List<EligibilityRequirement> listRequirements(UUID ruleId) {
        return getEntityManager()
                .createQuery("from EligibilityRequirement where ruleId = ?1 order by displayOrder",
                        EligibilityRequirement.class)
                .setParameter(1, ruleId)
                .getResultList();
    }
}
