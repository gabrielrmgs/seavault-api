package br.dev.irontech.seavault.vessels.api;

import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.profile.api.CurrentUser;
import br.dev.irontech.seavault.vessels.dto.VesselRequest;
import br.dev.irontech.seavault.vessels.dto.VesselResponse;
import br.dev.irontech.seavault.vessels.service.VesselService;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/vessels")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VesselResource {

    private final VesselService vesselService;
    private final CurrentUser currentUser;

    public VesselResource(VesselService vesselService, CurrentUser currentUser) {
        this.vesselService = vesselService;
        this.currentUser = currentUser;
    }

    @POST
    public Response create(@Valid VesselRequest req) {
        VesselResponse resp = vesselService.create(currentUser.id(), req);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    public PageResponse<VesselResponse> list(@QueryParam("page") Integer page,
                                             @QueryParam("size") Integer size) {
        return vesselService.list(currentUser.id(), PageRequest.of(page, size));
    }

    @GET
    @Path("/{id}")
    public VesselResponse get(@PathParam("id") UUID id) {
        return vesselService.get(currentUser.id(), id);
    }

    @PUT
    @Path("/{id}")
    public VesselResponse update(@PathParam("id") UUID id, @Valid VesselRequest req) {
        return vesselService.update(currentUser.id(), id, req);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        vesselService.delete(currentUser.id(), id);
        return Response.noContent().build();
    }
}
