package br.dev.irontech.seavault.reference.api;

import br.dev.irontech.seavault.reference.dto.CategoryDto;
import br.dev.irontech.seavault.reference.dto.CourseDto;
import br.dev.irontech.seavault.reference.dto.GroupDto;
import br.dev.irontech.seavault.reference.dto.TypeDto;
import br.dev.irontech.seavault.reference.service.ReferenceService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/reference")
@Produces(MediaType.APPLICATION_JSON)
public class ReferenceResource {

    private final ReferenceService service;

    public ReferenceResource(ReferenceService service) {
        this.service = service;
    }

    @GET
    @Path("/groups")
    public List<GroupDto> groups() {
        return service.groups();
    }

    @GET
    @Path("/categories")
    public List<CategoryDto> categories(@QueryParam("groupId") UUID groupId) {
        return service.categories(groupId);
    }

    @GET
    @Path("/course-catalog")
    public List<CourseDto> courses() {
        return service.courses();
    }

    @GET
    @Path("/types")
    public List<TypeDto> types(@QueryParam("kind") String kind) {
        return service.types(kind);
    }
}
