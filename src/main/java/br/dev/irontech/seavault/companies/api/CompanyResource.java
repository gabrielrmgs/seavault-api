package br.dev.irontech.seavault.companies.api;

import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.companies.dto.CompanyRequest;
import br.dev.irontech.seavault.companies.dto.CompanyResponse;
import br.dev.irontech.seavault.companies.service.CompanyService;
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

import java.util.UUID;

@Path("/api/companies")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CompanyResource {

    private final CompanyService companyService;
    private final CurrentUser currentUser;

    public CompanyResource(CompanyService companyService, CurrentUser currentUser) {
        this.companyService = companyService;
        this.currentUser = currentUser;
    }

    @POST
    public Response create(@Valid CompanyRequest req) {
        CompanyResponse resp = companyService.create(currentUser.id(), req);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    public PageResponse<CompanyResponse> list(@QueryParam("page") Integer page,
                                              @QueryParam("size") Integer size) {
        return companyService.list(currentUser.id(), PageRequest.of(page, size));
    }

    @GET
    @Path("/{id}")
    public CompanyResponse get(@PathParam("id") UUID id) {
        return companyService.get(currentUser.id(), id);
    }

    @PUT
    @Path("/{id}")
    public CompanyResponse update(@PathParam("id") UUID id, @Valid CompanyRequest req) {
        return companyService.update(currentUser.id(), id, req);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        companyService.delete(currentUser.id(), id);
        return Response.noContent().build();
    }
}
