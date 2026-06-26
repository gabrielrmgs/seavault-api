package br.dev.irontech.seavault.reference;

import br.dev.irontech.seavault.reference.domain.EligibilityRequirement;
import br.dev.irontech.seavault.reference.domain.EligibilityRule;
import br.dev.irontech.seavault.reference.domain.RequirementType;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EligibilityRulesSeedTest {

    @Inject
    ReferenceRepository referenceRepository;

    private UUID categoryIdByCode(String code) {
        return referenceRepository.listAllCategories().stream()
                .filter(c -> code.equals(c.code)).map(c -> c.id).findFirst().orElseThrow();
    }

    private UUID courseIdByCode(String code) {
        return referenceRepository.listCourses().stream()
                .filter(c -> code.equals(c.code)).map(c -> c.id).findFirst().orElseThrow();
    }

    @Test
    void categoryRuleSeededWithOrderedRequirements() {
        UUID mocoConves = categoryIdByCode("MOCO_CONVES");
        EligibilityRule rule = referenceRepository.findRuleByTargetCategory(mocoConves).orElseThrow();
        assertEquals("PROG_MOCO_CONVES", rule.code);

        List<EligibilityRequirement> reqs = referenceRepository.listRequirements(rule.id);
        assertEquals(3, reqs.size());
        assertEquals(RequirementType.CATEGORY, reqs.get(0).requirementType);
        assertEquals(RequirementType.SEATIME, reqs.get(1).requirementType);
        assertEquals(RequirementType.COURSE, reqs.get(2).requirementType);
        assertEquals(180, reqs.get(1).requiredDays);
    }

    @Test
    void secondCategoryRuleSeeded() {
        UUID marinheiroConves = categoryIdByCode("MARINHEIRO_CONVES");
        EligibilityRule rule = referenceRepository.findRuleByTargetCategory(marinheiroConves).orElseThrow();
        assertEquals("PROG_MARINHEIRO_CONVES", rule.code);

        List<EligibilityRequirement> reqs = referenceRepository.listRequirements(rule.id);
        assertEquals(3, reqs.size());
        assertEquals(365, reqs.get(1).requiredDays);
    }

    @Test
    void courseTargetRuleSeeded() {
        UUID caci = courseIdByCode("CACI");
        EligibilityRule rule = referenceRepository.findRuleByTargetCourse(caci).orElseThrow();
        assertEquals("HABILITA_CACI", rule.code);

        List<EligibilityRequirement> reqs = referenceRepository.listRequirements(rule.id);
        assertEquals(1, reqs.size());
        assertEquals(RequirementType.COURSE, reqs.get(0).requirementType);
        assertTrue(reqs.get(0).requiredCourseId != null);
    }
}
