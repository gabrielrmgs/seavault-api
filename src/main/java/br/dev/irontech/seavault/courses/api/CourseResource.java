package br.dev.irontech.seavault.courses.api;

import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.courses.dto.CourseRequest;
import br.dev.irontech.seavault.courses.dto.CourseResponse;
import br.dev.irontech.seavault.courses.service.CourseService;
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

@Path("/api/courses")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {

    private final CourseService courseService;
    private final CurrentUser currentUser;

    public CourseResource(CourseService courseService, CurrentUser currentUser) {
        this.courseService = courseService;
        this.currentUser = currentUser;
    }

    @POST
    public Response create(@Valid CourseRequest req) {
        CourseResponse resp = courseService.create(currentUser.id(), req);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    public PageResponse<CourseResponse> list(@QueryParam("page") Integer page,
                                             @QueryParam("size") Integer size) {
        return courseService.list(currentUser.id(), PageRequest.of(page, size));
    }

    @GET
    @Path("/{id}")
    public CourseResponse get(@PathParam("id") UUID id) {
        return courseService.get(currentUser.id(), id);
    }

    @PUT
    @Path("/{id}")
    public CourseResponse update(@PathParam("id") UUID id, @Valid CourseRequest req) {
        return courseService.update(currentUser.id(), id, req);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        courseService.delete(currentUser.id(), id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/files")
    public Response attach(@PathParam("id") UUID id, @Valid AttachFileRequest req) {
        courseService.attachFile(currentUser.id(), id, req.fileId());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}/files/{fileId}")
    public Response detach(@PathParam("id") UUID id, @PathParam("fileId") UUID fileId) {
        courseService.detachFile(currentUser.id(), id, fileId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/files")
    public List<FileResponse> files(@PathParam("id") UUID id) {
        return courseService.listFiles(currentUser.id(), id);
    }
}
