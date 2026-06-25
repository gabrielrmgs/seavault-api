package br.dev.irontech.seavault.auth.api;

import br.dev.irontech.seavault.auth.dto.ConfirmEmailRequest;
import br.dev.irontech.seavault.auth.dto.LoginRequest;
import br.dev.irontech.seavault.auth.dto.PasswordResetConfirm;
import br.dev.irontech.seavault.auth.dto.PasswordResetRequest;
import br.dev.irontech.seavault.auth.dto.RefreshRequest;
import br.dev.irontech.seavault.auth.dto.RegisterRequest;
import br.dev.irontech.seavault.auth.dto.RegisterResponse;
import br.dev.irontech.seavault.auth.dto.TokenResponse;
import br.dev.irontech.seavault.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest req) {
        RegisterResponse resp = authService.register(req);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @POST
    @Path("/confirm-email")
    public Response confirmEmail(@Valid ConfirmEmailRequest req) {
        authService.confirmEmail(req.token());
        return Response.noContent().build();
    }

    @POST
    @Path("/login")
    public TokenResponse login(@Valid LoginRequest req) {
        return authService.login(req);
    }

    @POST
    @Path("/refresh")
    public TokenResponse refresh(@Valid RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @POST
    @Path("/logout")
    public Response logout(@Valid RefreshRequest req) {
        authService.logout(req.refreshToken());
        return Response.noContent().build();
    }

    @POST
    @Path("/request-password-reset")
    public Response requestPasswordReset(@Valid PasswordResetRequest req) {
        authService.requestPasswordReset(req.email());
        return Response.accepted().build();
    }

    @POST
    @Path("/reset-password")
    public Response resetPassword(@Valid PasswordResetConfirm req) {
        authService.resetPassword(req);
        return Response.noContent().build();
    }
}
