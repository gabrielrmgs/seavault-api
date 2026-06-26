package br.dev.irontech.seavault.eligibility.service;

import br.dev.irontech.seavault.common.error.BadRequestException;
import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.courses.repo.CourseRepository;
import br.dev.irontech.seavault.eligibility.dto.EligibilityResponse;
import br.dev.irontech.seavault.eligibility.dto.EligibilityResponse.RequirementResult;
import br.dev.irontech.seavault.profile.domain.Profile;
import br.dev.irontech.seavault.profile.repo.ProfileRepository;
import br.dev.irontech.seavault.reference.domain.Category;
import br.dev.irontech.seavault.reference.domain.CourseCatalog;
import br.dev.irontech.seavault.reference.domain.EligibilityRequirement;
import br.dev.irontech.seavault.reference.domain.EligibilityRule;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.seatime.service.SeatimeService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class EligibilityService {

    private final ReferenceRepository referenceRepository;
    private final ProfileRepository profileRepository;
    private final CourseRepository courseRepository;
    private final SeatimeService seatimeService;

    public EligibilityService(ReferenceRepository referenceRepository,
                              ProfileRepository profileRepository,
                              CourseRepository courseRepository,
                              SeatimeService seatimeService) {
        this.referenceRepository = referenceRepository;
        this.profileRepository = profileRepository;
        this.courseRepository = courseRepository;
        this.seatimeService = seatimeService;
    }

    public EligibilityResponse evaluate(UUID userId, UUID targetCategoryId, UUID targetCourseId) {
        if (targetCategoryId != null && targetCourseId != null) {
            throw new BadRequestException("Informe apenas um alvo: targetCategoryId ou targetCourseId");
        }

        Profile profile = profileRepository.findActiveByUserId(userId).orElse(null);

        if (targetCategoryId == null && targetCourseId == null) {
            if (profile == null || profile.targetCategoryId == null) {
                throw new BusinessException("Nenhum alvo informado e o perfil nao tem categoria-alvo definida");
            }
            targetCategoryId = profile.targetCategoryId;
        }

        EligibilityRule rule;
        String targetName;
        if (targetCategoryId != null) {
            UUID cat = targetCategoryId;
            rule = referenceRepository.findRuleByTargetCategory(cat)
                    .orElseThrow(() -> new NotFoundException("Nenhuma regra de elegibilidade para a categoria: " + cat));
            targetName = referenceRepository.findCategoryById(cat).map(c -> c.name).orElse(null);
        } else {
            UUID course = targetCourseId;
            rule = referenceRepository.findRuleByTargetCourse(course)
                    .orElseThrow(() -> new NotFoundException("Nenhuma regra de elegibilidade para o curso: " + course));
            targetName = referenceRepository.findCourseById(course).map(c -> c.name).orElse(null);
        }

        List<EligibilityRequirement> requirements = referenceRepository.listRequirements(rule.id);
        Set<UUID> completedCatalogCourses = courseRepository.listCompletedByUser(userId).stream()
                .map(c -> c.catalogCourseId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        long totalSeatimeDays = seatimeService.summary(userId).totalDays();
        Category currentCategory = (profile != null && profile.categoryId != null)
                ? referenceRepository.findCategoryById(profile.categoryId).orElse(null)
                : null;

        List<RequirementResult> results = requirements.stream()
                .map(req -> evaluateRequirement(req, completedCatalogCourses, totalSeatimeDays, currentCategory))
                .toList();

        boolean eligible = results.stream().allMatch(RequirementResult::cumprido);
        return new EligibilityResponse(eligible, rule.targetCategoryId, rule.targetCourseId, targetName, results);
    }

    private RequirementResult evaluateRequirement(EligibilityRequirement req,
                                                  Set<UUID> completedCatalogCourses,
                                                  long totalSeatimeDays,
                                                  Category currentCategory) {
        return switch (req.requirementType) {
            case COURSE -> evaluateCourse(req, completedCatalogCourses);
            case SEATIME -> evaluateSeatime(req, totalSeatimeDays);
            case CATEGORY -> evaluateCategory(req, currentCategory);
        };
    }

    private RequirementResult evaluateCourse(EligibilityRequirement req, Set<UUID> completedCatalogCourses) {
        CourseCatalog course = referenceRepository.findCourseById(req.requiredCourseId).orElse(null);
        String label = course != null ? course.code + " — " + course.name : "curso exigido";
        boolean met = completedCatalogCourses.contains(req.requiredCourseId);
        return new RequirementResult("COURSE", label, met ? "concluído" : "não possui", met);
    }

    private static RequirementResult evaluateSeatime(EligibilityRequirement req, long totalSeatimeDays) {
        int required = req.requiredDays != null ? req.requiredDays : 0;
        boolean met = totalSeatimeDays >= required;
        return new RequirementResult("SEATIME", required + " dias", totalSeatimeDays + " dias", met);
    }

    private RequirementResult evaluateCategory(EligibilityRequirement req, Category currentCategory) {
        Category required = referenceRepository.findCategoryById(req.requiredCategoryId).orElse(null);
        String exigido = required != null ? required.name : "categoria exigida";
        boolean met = currentCategory != null && required != null
                && currentCategory.progressionOrder >= required.progressionOrder;
        String atual = currentCategory != null ? currentCategory.name : "—";
        return new RequirementResult("CATEGORY", exigido, atual, met);
    }
}
