package br.dev.irontech.seavault.reference.service;

import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.reference.dto.CategoryDto;
import br.dev.irontech.seavault.reference.dto.GroupDto;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ReferenceServiceTest {

    @Inject
    ReferenceService service;

    @Test
    void groupsReturnsSeedAsDtos() {
        List<GroupDto> groups = service.groups();
        assertEquals(5, groups.size());
        assertTrue(groups.stream().anyMatch(g -> g.code().equals("MARITIMOS")));
    }

    @Test
    void categoriesWithNullGroupReturnsAll() {
        assertFalse(service.categories(null).isEmpty());
    }

    @Test
    void categoriesFilteredByGroup() {
        GroupDto maritimos = service.groups().stream()
                .filter(g -> g.code().equals("MARITIMOS")).findFirst().orElseThrow();
        List<CategoryDto> cats = service.categories(maritimos.id());
        assertFalse(cats.isEmpty());
        assertTrue(cats.stream().allMatch(c -> c.groupId().equals(maritimos.id())));
    }

    @Test
    void typesNormalizesKindToUppercase() {
        assertFalse(service.types("document").isEmpty());
    }

    @Test
    void typesRejectsBlankKind() {
        assertThrows(BusinessException.class, () -> service.types(" "));
    }
}
