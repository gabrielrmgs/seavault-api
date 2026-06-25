package br.dev.irontech.seavault.certificates.api;

import br.dev.irontech.seavault.certificates.dto.CertificateRequest;
import br.dev.irontech.seavault.certificates.dto.CertificateResponse;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.files.dto.AttachFileRequest;
import br.dev.irontech.seavault.files.dto.FileResponse;
import br.dev.irontech.seavault.profile.api.CurrentUser;
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

@Path("/api/certificates")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CertificateResource {

    private final CertificateService certificateService;
    private final CurrentUser currentUser;

    public CertificateResource(CertificateService certificateService, CurrentUser currentUser) {
        this.certificateService = certificateService;
        this.currentUser = currentUser;
    }

    @POST
    public Response create(@Valid CertificateRequest req) {
        CertificateResponse resp = certificateService.create(currentUser.id(), req);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    public PageResponse<CertificateResponse> list(@QueryParam("page") Integer page,
                                                  @QueryParam("size") Integer size) {
        return certificateService.list(currentUser.id(), PageRequest.of(page, size));
    }

    @GET
    @Path("/{id}")
    public CertificateResponse get(@PathParam("id") UUID id) {
        return certificateService.get(currentUser.id(), id);
    }

    @PUT
    @Path("/{id}")
    public CertificateResponse update(@PathParam("id") UUID id, @Valid CertificateRequest req) {
        return certificateService.update(currentUser.id(), id, req);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        certificateService.delete(currentUser.id(), id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/files")
    public Response attach(@PathParam("id") UUID id, @Valid AttachFileRequest req) {
        certificateService.attachFile(currentUser.id(), id, req.fileId());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}/files/{fileId}")
    public Response detach(@PathParam("id") UUID id, @PathParam("fileId") UUID fileId) {
        certificateService.detachFile(currentUser.id(), id, fileId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/files")
    public List<FileResponse> files(@PathParam("id") UUID id) {
        return certificateService.listFiles(currentUser.id(), id);
    }
}
