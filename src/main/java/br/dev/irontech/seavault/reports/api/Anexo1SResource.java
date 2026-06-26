package br.dev.irontech.seavault.reports.api;

import br.dev.irontech.seavault.profile.api.CurrentUser;
import br.dev.irontech.seavault.reports.domain.ReportFormat;
import br.dev.irontech.seavault.reports.dto.Anexo1SRequest;
import br.dev.irontech.seavault.reports.pdf.PdfRenderer;
import br.dev.irontech.seavault.reports.pdf.ReportDocument;
import br.dev.irontech.seavault.reports.service.Anexo1SService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/reports/anexo-1s")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.APPLICATION_JSON, "application/pdf"})
public class Anexo1SResource {

    private final Anexo1SService anexo1SService;
    private final PdfRenderer pdfRenderer;
    private final CurrentUser currentUser;

    public Anexo1SResource(Anexo1SService anexo1SService, PdfRenderer pdfRenderer, CurrentUser currentUser) {
        this.anexo1SService = anexo1SService;
        this.pdfRenderer = pdfRenderer;
        this.currentUser = currentUser;
    }

    @POST
    public Response generate(@QueryParam("format") String format, Anexo1SRequest req) {
        ReportFormat reportFormat = (format == null || format.isBlank())
                ? ReportFormat.PDF
                : ReportFormat.fromString(format);

        ReportDocument doc = anexo1SService.generate(currentUser.id(), req, reportFormat);
        if (reportFormat == ReportFormat.PDF) {
            byte[] pdf = pdfRenderer.render(doc);
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"anexo-1s.pdf\"")
                    .build();
        }
        return Response.ok(doc).build();
    }
}
