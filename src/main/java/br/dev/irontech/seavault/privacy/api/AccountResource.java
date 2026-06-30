package br.dev.irontech.seavault.privacy.api;

import br.dev.irontech.seavault.common.http.ContentDisposition;
import br.dev.irontech.seavault.privacy.service.AccountPrivacyService;
import br.dev.irontech.seavault.profile.api.CurrentUser;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/account")
@Authenticated
public class AccountResource {

    private final AccountPrivacyService privacyService;
    private final CurrentUser currentUser;

    public AccountResource(AccountPrivacyService privacyService, CurrentUser currentUser) {
        this.privacyService = privacyService;
        this.currentUser = currentUser;
    }

    @DELETE
    public Response delete() {
        privacyService.deleteAccount(currentUser.id());
        return Response.noContent().build();
    }

    @GET
    @Path("/export")
    @Produces(MediaType.APPLICATION_JSON)
    public Response export() {
        return Response.ok(privacyService.exportData(currentUser.id()))
                .header("Content-Disposition", ContentDisposition.attachment("seavault-meus-dados.json"))
                .build();
    }
}
