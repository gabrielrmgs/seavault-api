package br.dev.irontech.seavault.reference;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ReferenceSeedTest {

    @Inject
    EntityManager em;

    private long count(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }

    @Test
    @Transactional
    void groupsSeeded() {
        assertEquals(5L, count("SELECT count(*) FROM ref_professional_groups"));
        assertEquals(1L, count(
                "SELECT count(*) FROM ref_professional_groups WHERE code = 'MARITIMOS'"));
    }

    @Test
    @Transactional
    void categoriesLinkedToGroup() {
        assertTrue(count("SELECT count(*) FROM ref_categories") >= 11);
        // Toda categoria aponta para um grupo existente (integridade do subselect do seed).
        assertEquals(0L, count(
                "SELECT count(*) FROM ref_categories c "
              + "LEFT JOIN ref_professional_groups g ON g.id = c.group_id "
              + "WHERE g.id IS NULL"));
    }

    @Test
    @Transactional
    void coursesAndTypesSeeded() {
        assertEquals(5L, count("SELECT count(*) FROM ref_course_catalog"));
        assertTrue(count("SELECT count(*) FROM ref_types WHERE kind = 'DOCUMENT'") >= 5);
        assertTrue(count("SELECT count(*) FROM ref_types WHERE kind = 'VESSEL'") >= 4);
        assertTrue(count("SELECT count(*) FROM ref_types WHERE kind = 'COMPANY'") >= 3);
    }
}
