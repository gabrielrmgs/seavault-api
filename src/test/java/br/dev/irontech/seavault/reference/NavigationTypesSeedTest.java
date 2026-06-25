package br.dev.irontech.seavault.reference;

import br.dev.irontech.seavault.reference.domain.RefType;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class NavigationTypesSeedTest {

    @Inject
    ReferenceRepository referenceRepository;

    @Test
    void navigationTypesAreSeeded() {
        List<RefType> types = referenceRepository.listTypes("NAVIGATION");
        assertEquals(4, types.size());
        assertTrue(types.stream().anyMatch(t -> "MAR_ABERTO".equals(t.code)));
    }
}
