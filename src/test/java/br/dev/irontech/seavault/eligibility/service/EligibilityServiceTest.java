package br.dev.irontech.seavault.eligibility.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserPlan;
import br.dev.irontech.seavault.auth.domain.UserRole;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.BadRequestException;
import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.courses.domain.Course;
import br.dev.irontech.seavault.courses.domain.CourseStatus;
import br.dev.irontech.seavault.courses.repo.CourseRepository;
import br.dev.irontech.seavault.eligibility.dto.EligibilityResponse;
import br.dev.irontech.seavault.profile.domain.Profile;
import br.dev.irontech.seavault.profile.repo.ProfileRepository;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.voyages.dto.VoyageRequest;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EligibilityServiceTest {

    @Inject
    EligibilityService eligibilityService;

    @Inject
    UserRepository userRepository;

    @Inject
    ProfileRepository profileRepository;

    @Inject
    CourseRepository courseRepository;

    @Inject
    VoyageService voyageService;

    @Inject
    ReferenceRepository referenceRepository;

    @Transactional
    UUID newUser(String email) {
        User u = new User();
        u.name = "Marinheiro";
        u.email = email;
        u.passwordHash = "hash";
        u.status = UserStatus.ATIVO;
        u.plan = UserPlan.FREE;
        u.role = UserRole.USER;
        userRepository.persist(u);
        return u.id;
    }

    private UUID categoryIdByCode(String code) {
        return referenceRepository.listAllCategories().stream()
                .filter(c -> code.equals(c.code)).map(c -> c.id).findFirst().orElseThrow();
    }

    private UUID courseIdByCode(String code) {
        return referenceRepository.listCourses().stream()
                .filter(c -> code.equals(c.code)).map(c -> c.id).findFirst().orElseThrow();
    }

    @Transactional
    void setProfileCategory(UUID userId, UUID categoryId, UUID targetCategoryId) {
        Profile p = new Profile();
        p.userId = userId;
        p.categoryId = categoryId;
        p.targetCategoryId = targetCategoryId;
        p.completionPercent = 0;
        profileRepository.persist(p);
    }

    @Transactional
    void addCompletedCourse(UUID userId, UUID catalogCourseId) {
        addCourse(userId, catalogCourseId, CourseStatus.CONCLUIDO);
    }

    @Transactional
    void addCourse(UUID userId, UUID catalogCourseId, CourseStatus status) {
        Course c = new Course();
        c.userId = userId;
        c.name = "Curso";
        c.catalogCourseId = catalogCourseId;
        c.status = status;
        courseRepository.persist(c);
    }

    private void addSeatime(UUID userId) {
        voyageService.create(userId, new VoyageRequest(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                null, null, null, null, "Marinheiro", null, null, null, null, null));
    }

    @Test
    void allRequirementsMetIsEligible() {
        UUID userId = newUser("elig-all@example.com");
        setProfileCategory(userId, categoryIdByCode("MOCO_CONVES"), null);
        addSeatime(userId);
        addCompletedCourse(userId, courseIdByCode("CBSP"));

        EligibilityResponse resp = eligibilityService.evaluate(userId, categoryIdByCode("MOCO_CONVES"), null);

        assertTrue(resp.eligible());
        assertEquals(3, resp.requirements().size());
        assertTrue(resp.requirements().stream().allMatch(EligibilityResponse.RequirementResult::cumprido));
    }

    @Test
    void seatimeNotMetIsNotEligible() {
        UUID userId = newUser("elig-seatime@example.com");
        setProfileCategory(userId, categoryIdByCode("MOCO_CONVES"), null);
        addCompletedCourse(userId, courseIdByCode("CBSP"));

        EligibilityResponse resp = eligibilityService.evaluate(userId, categoryIdByCode("MOCO_CONVES"), null);

        assertFalse(resp.eligible());
        assertFalse(resp.requirements().stream()
                .filter(r -> "SEATIME".equals(r.type())).findFirst().orElseThrow().cumprido());
    }

    @Test
    void courseNotMetIsNotEligible() {
        UUID userId = newUser("elig-course@example.com");
        setProfileCategory(userId, categoryIdByCode("MOCO_CONVES"), null);
        addSeatime(userId);

        EligibilityResponse resp = eligibilityService.evaluate(userId, categoryIdByCode("MOCO_CONVES"), null);

        assertFalse(resp.eligible());
        assertFalse(resp.requirements().stream()
                .filter(r -> "COURSE".equals(r.type())).findFirst().orElseThrow().cumprido());
    }

    @Test
    void categoryNotMetWhenProfileHasNoCategory() {
        UUID userId = newUser("elig-cat@example.com");
        addSeatime(userId);
        addCompletedCourse(userId, courseIdByCode("CBSP"));

        EligibilityResponse resp = eligibilityService.evaluate(userId, categoryIdByCode("MOCO_CONVES"), null);

        assertFalse(resp.eligible());
        EligibilityResponse.RequirementResult cat = resp.requirements().stream()
                .filter(r -> "CATEGORY".equals(r.type())).findFirst().orElseThrow();
        assertFalse(cat.cumprido());
        assertEquals("—", cat.atual());
    }

    @Test
    void fallsBackToProfileTargetCategory() {
        UUID userId = newUser("elig-fallback@example.com");
        UUID moco = categoryIdByCode("MOCO_CONVES");
        setProfileCategory(userId, categoryIdByCode("MAR_AUX_CONVES"), moco);

        EligibilityResponse resp = eligibilityService.evaluate(userId, null, null);

        assertEquals(moco, resp.targetCategoryId());
    }

    @Test
    void noTargetResolvedThrows422() {
        UUID userId = newUser("elig-notarget@example.com");
        assertThrows(BusinessException.class, () -> eligibilityService.evaluate(userId, null, null));
    }

    @Test
    void bothTargetsThrows400() {
        UUID userId = newUser("elig-both@example.com");
        assertThrows(BadRequestException.class, () -> eligibilityService.evaluate(
                userId, categoryIdByCode("MOCO_CONVES"), courseIdByCode("CACI")));
    }

    @Test
    void unknownTargetThrows404() {
        UUID userId = newUser("elig-unknown@example.com");
        UUID unknown = UUID.fromString("00000000-0000-0000-0000-000000000000");
        assertThrows(NotFoundException.class, () -> eligibilityService.evaluate(userId, unknown, null));
    }

    @Test
    void courseTargetRuleEvaluates() {
        UUID userId = newUser("elig-coursetarget@example.com");
        addCompletedCourse(userId, courseIdByCode("STCW_BST"));

        EligibilityResponse resp = eligibilityService.evaluate(userId, null, courseIdByCode("CACI"));

        assertTrue(resp.eligible());
        assertEquals(1, resp.requirements().size());
        assertEquals("COURSE", resp.requirements().get(0).type());
    }

    @Test
    void plannedCourseDoesNotSatisfyCourseRequirement() {
        UUID userId = newUser("elig-planned-course@example.com");
        addCourse(userId, courseIdByCode("STCW_BST"), CourseStatus.EM_ANDAMENTO);

        EligibilityResponse resp = eligibilityService.evaluate(userId, null, courseIdByCode("CACI"));

        assertFalse(resp.eligible());
        assertFalse(resp.requirements().get(0).cumprido());
    }

    @Test
    void otherUsersCompletedCourseDoesNotSatisfyRequirement() {
        UUID userId = newUser("elig-course-iso-a@example.com");
        UUID otherUserId = newUser("elig-course-iso-b@example.com");
        addCompletedCourse(otherUserId, courseIdByCode("STCW_BST"));

        EligibilityResponse resp = eligibilityService.evaluate(userId, null, courseIdByCode("CACI"));

        assertFalse(resp.eligible());
        assertFalse(resp.requirements().get(0).cumprido());
    }
}
