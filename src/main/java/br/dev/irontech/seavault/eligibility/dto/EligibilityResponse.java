package br.dev.irontech.seavault.eligibility.dto;

import java.util.List;
import java.util.UUID;

public record EligibilityResponse(
        boolean eligible,
        UUID targetCategoryId,
        UUID targetCourseId,
        String targetName,
        List<RequirementResult> requirements
) {
    public record RequirementResult(String type, String exigido, String atual, boolean cumprido) {}
}
