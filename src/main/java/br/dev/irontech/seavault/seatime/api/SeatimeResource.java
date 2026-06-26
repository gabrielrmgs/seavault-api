package br.dev.irontech.seavault.seatime.api;

import br.dev.irontech.seavault.profile.api.CurrentUser;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse;
import br.dev.irontech.seavault.seatime.service.SeatimeService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/seatime")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class SeatimeResource {

    private final SeatimeService seatimeService;
    private final CurrentUser currentUser;

    public SeatimeResource(SeatimeService seatimeService, CurrentUser currentUser) {
        this.seatimeService = seatimeService;
        this.currentUser = currentUser;
    }

    @GET
    public SeatimeSummaryResponse summary() {
        return seatimeService.summary(currentUser.id());
    }
}
