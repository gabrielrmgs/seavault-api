package br.dev.irontech.seavault.reference.repo;

import br.dev.irontech.seavault.reference.domain.CourseCatalog;
import br.dev.irontech.seavault.reference.domain.RefType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ReferenceLookupTest {

    @Inject
    ReferenceRepository repo;

    @Test
    void findsSeededDocumentType() {
        RefType cir = repo.listTypes("DOCUMENT").stream()
                .filter(t -> t.code.equals("CIR")).findFirst().orElseThrow();
        assertTrue(repo.findTypeById(cir.id).isPresent());
    }

    @Test
    void findTypeByUnknownIdIsEmpty() {
        assertFalse(repo.findTypeById(UUID.fromString("00000000-0000-0000-0000-000000000000")).isPresent());
    }

    @Test
    void findsSeededCatalogCourse() {
        CourseCatalog bst = repo.listCourses().stream()
                .filter(c -> c.code.equals("STCW_BST")).findFirst().orElseThrow();
        assertTrue(repo.findCourseById(bst.id).isPresent());
    }

    @Test
    void findCourseByUnknownIdIsEmpty() {
        assertFalse(repo.findCourseById(UUID.fromString("00000000-0000-0000-0000-000000000000")).isPresent());
    }
}
