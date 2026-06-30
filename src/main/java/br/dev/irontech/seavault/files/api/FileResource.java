package br.dev.irontech.seavault.files.api;

import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.http.ContentDisposition;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.files.dto.FileDownload;
import br.dev.irontech.seavault.files.dto.FileResponse;
import br.dev.irontech.seavault.files.service.FileService;
import br.dev.irontech.seavault.profile.api.CurrentUser;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Path("/api/files")
@Authenticated
public class FileResource {

    private final FileService fileService;
    private final CurrentUser currentUser;

    public FileResource(FileService fileService, CurrentUser currentUser) {
        this.fileService = fileService;
        this.currentUser = currentUser;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@RestForm("file") FileUpload file) {
        if (file == null) {
            throw new BusinessException("Campo 'file' e obrigatorio");
        }
        byte[] content;
        try {
            content = Files.readAllBytes(file.uploadedFile());
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler upload", e);
        }
        FileResponse resp = fileService.upload(
                currentUser.id(), file.fileName(), file.contentType(), content);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PageResponse<FileResponse> list(@QueryParam("page") Integer page,
                                           @QueryParam("size") Integer size) {
        return fileService.list(currentUser.id(), PageRequest.of(page, size));
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public FileResponse get(@PathParam("id") UUID id) {
        return fileService.get(currentUser.id(), id);
    }

    @GET
    @Path("/{id}/content")
    public Response content(@PathParam("id") UUID id) {
        FileDownload d = fileService.download(currentUser.id(), id);
        return Response.ok(d.content())
                .type(d.contentType())
                .header("Content-Disposition", ContentDisposition.attachment(d.originalName()))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        fileService.delete(currentUser.id(), id);
        return Response.noContent().build();
    }
}
