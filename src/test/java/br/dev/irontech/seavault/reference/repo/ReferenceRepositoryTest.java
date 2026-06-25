package br.dev.irontech.seavault.reference.repo;

import br.dev.irontech.seavault.reference.domain.Category;
import br.dev.irontech.seavault.reference.domain.ProfessionalGroup;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ReferenceRepositoryTest {

    @Inject
    ReferenceRepository repo;

    @Test
    void listsGroupsInDisplayOrder() {
        List<ProfessionalGroup> groups = repo.listGroups();
        assertEquals(5, groups.size());
        assertEquals("MARITIMOS", groups.get(0).code); // display_order = 1
        // ordenação não-decrescente
        for (int i = 1; i < groups.size(); i++) {
            assertTrue(groups.get(i).displayOrder >= groups.get(i - 1).displayOrder);
        }
    }

    @Test
    void listsCategoriesByGroup() {
        ProfessionalGroup maritimos = repo.listGroups().stream()
                .filter(g -> g.code.equals("MARITIMOS")).findFirst().orElseThrow();
        List<Category> cats = repo.listCategoriesByGroup(maritimos.id);
        assertFalse(cats.isEmpty());
        assertTrue(cats.stream().allMatch(c -> c.groupId.equals(maritimos.id)));
    }

    @Test
    void listsTypesFilteredByKind() {
        assertFalse(repo.listTypes("DOCUMENT").isEmpty());
        assertTrue(repo.listTypes("DOCUMENT").stream().allMatch(t -> t.kind.equals("DOCUMENT")));
        assertTrue(repo.listTypes("INEXISTENTE").isEmpty());
    }

    @Test
    void listsCourses() {
        assertEquals(5, repo.listCourses().size());
    }
}
