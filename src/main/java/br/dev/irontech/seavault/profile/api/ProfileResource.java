package br.dev.irontech.seavault.profile.api;

import br.dev.irontech.seavault.profile.dto.ProfileResponse;
import br.dev.irontech.seavault.profile.dto.ProfileUpdateRequest;
import br.dev.irontech.seavault.profile.service.ProfileService;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/profile")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource {

    private final ProfileService profileService;
    private final CurrentUser currentUser;

    public ProfileResource(ProfileService profileService, CurrentUser currentUser) {
        this.profileService = profileService;
        this.currentUser = currentUser;
    }

    @GET
    public ProfileResponse get() {
        return profileService.getOrCreate(currentUser.id());
    }

    @PUT
    public ProfileResponse update(@Valid ProfileUpdateRequest req) {
        return profileService.update(currentUser.id(), req);
    }
}
