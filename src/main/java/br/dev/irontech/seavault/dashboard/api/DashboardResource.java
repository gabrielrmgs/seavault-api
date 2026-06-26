package br.dev.irontech.seavault.dashboard.api;

import br.dev.irontech.seavault.dashboard.dto.DashboardResponse;
import br.dev.irontech.seavault.dashboard.service.DashboardService;
import br.dev.irontech.seavault.profile.api.CurrentUser;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/dashboard")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class DashboardResource {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    public DashboardResource(DashboardService dashboardService, CurrentUser currentUser) {
        this.dashboardService = dashboardService;
        this.currentUser = currentUser;
    }

    @GET
    public DashboardResponse get() {
        return dashboardService.summary(currentUser.id());
    }
}
