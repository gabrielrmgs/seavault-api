package br.dev.irontech.seavault.eligibility.api;

import br.dev.irontech.seavault.eligibility.dto.EligibilityResponse;
import br.dev.irontech.seavault.eligibility.service.EligibilityService;
import br.dev.irontech.seavault.profile.api.CurrentUser;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api/eligibility")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class EligibilityResource {

    private final EligibilityService eligibilityService;
    private final CurrentUser currentUser;

    public EligibilityResource(EligibilityService eligibilityService, CurrentUser currentUser) {
        this.eligibilityService = eligibilityService;
        this.currentUser = currentUser;
    }

    @GET
    public EligibilityResponse evaluate(@QueryParam("targetCategoryId") UUID targetCategoryId,
                                        @QueryParam("targetCourseId") UUID targetCourseId) {
        return eligibilityService.evaluate(currentUser.id(), targetCategoryId, targetCourseId);
    }
}
