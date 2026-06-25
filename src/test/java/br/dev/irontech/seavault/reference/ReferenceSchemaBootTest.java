package br.dev.irontech.seavault.reference;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ReferenceSchemaBootTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    void referenceTablesExist() {
        // Não assume contagem (o seed da Task 2 ainda não existe quando esta task roda
        // isolada); apenas garante que as tabelas existem e são consultáveis.
        for (String table : new String[]{
                "ref_professional_groups", "ref_categories", "ref_course_catalog", "ref_types"}) {
            Object count = em.createNativeQuery("SELECT count(*) FROM " + table).getSingleResult();
            assertTrue(((Number) count).longValue() >= 0, table + " deve existir");
        }
    }
}
