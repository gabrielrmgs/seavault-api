package br.dev.irontech.seavault.voyages.api;

import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.files.dto.AttachFileRequest;
import br.dev.irontech.seavault.files.dto.FileResponse;
import br.dev.irontech.seavault.profile.api.CurrentUser;
import br.dev.irontech.seavault.voyages.dto.VoyageRequest;
import br.dev.irontech.seavault.voyages.dto.VoyageResponse;
import br.dev.irontech.seavault.voyages.service.VoyageService;
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

import java.util.List;
import java.util.UUID;

@Path("/api/voyages")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VoyageResource {

    private final VoyageService voyageService;
    private final CurrentUser currentUser;

    public VoyageResource(VoyageService voyageService, CurrentUser currentUser) {
        this.voyageService = voyageService;
        this.currentUser = currentUser;
    }

    @POST
    public Response create(@Valid VoyageRequest req) {
        VoyageResponse resp = voyageService.create(currentUser.id(), req);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    public PageResponse<VoyageResponse> list(@QueryParam("page") Integer page,
                                             @QueryParam("size") Integer size) {
        return voyageService.list(currentUser.id(), PageRequest.of(page, size));
    }

    @GET
    @Path("/{id}")
    public VoyageResponse get(@PathParam("id") UUID id) {
        return voyageService.get(currentUser.id(), id);
    }

    @PUT
    @Path("/{id}")
    public VoyageResponse update(@PathParam("id") UUID id, @Valid VoyageRequest req) {
        return voyageService.update(currentUser.id(), id, req);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        voyageService.delete(currentUser.id(), id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/files")
    public Response attach(@PathParam("id") UUID id, @Valid AttachFileRequest req) {
        voyageService.attachFile(currentUser.id(), id, req.fileId());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}/files/{fileId}")
    public Response detach(@PathParam("id") UUID id, @PathParam("fileId") UUID fileId) {
        voyageService.detachFile(currentUser.id(), id, fileId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/files")
    public List<FileResponse> files(@PathParam("id") UUID id) {
        return voyageService.listFiles(currentUser.id(), id);
    }
}
