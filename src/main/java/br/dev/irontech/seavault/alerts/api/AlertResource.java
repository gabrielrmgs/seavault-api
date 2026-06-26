package br.dev.irontech.seavault.alerts.api;

import br.dev.irontech.seavault.alerts.domain.AlertStatus;
import br.dev.irontech.seavault.alerts.dto.AlertResponse;
import br.dev.irontech.seavault.alerts.dto.AlertStatusRequest;
import br.dev.irontech.seavault.alerts.service.AlertService;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.profile.api.CurrentUser;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api/alerts")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource {

    private final AlertService alertService;
    private final CurrentUser currentUser;

    public AlertResource(AlertService alertService, CurrentUser currentUser) {
        this.alertService = alertService;
        this.currentUser = currentUser;
    }

    @GET
    public PageResponse<AlertResponse> list(@QueryParam("status") AlertStatus status,
                                            @QueryParam("page") Integer page,
                                            @QueryParam("size") Integer size) {
        return alertService.list(currentUser.id(), status, PageRequest.of(page, size));
    }

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public AlertResponse changeStatus(@PathParam("id") UUID id, @Valid AlertStatusRequest req) {
        return alertService.changeStatus(currentUser.id(), id, req.status());
    }
}
