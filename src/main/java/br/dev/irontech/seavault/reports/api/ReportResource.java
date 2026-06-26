package br.dev.irontech.seavault.reports.api;

import br.dev.irontech.seavault.profile.api.CurrentUser;
import br.dev.irontech.seavault.reports.domain.ReportFormat;
import br.dev.irontech.seavault.reports.domain.ReportType;
import br.dev.irontech.seavault.reports.dto.ReportHistoryResponse;
import br.dev.irontech.seavault.reports.dto.ReportOptions;
import br.dev.irontech.seavault.reports.pdf.PdfRenderer;
import br.dev.irontech.seavault.reports.pdf.ReportDocument;
import br.dev.irontech.seavault.reports.service.ReportService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Path("/api/reports")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class ReportResource {

    private final ReportService reportService;
    private final PdfRenderer pdfRenderer;
    private final CurrentUser currentUser;

    public ReportResource(ReportService reportService, PdfRenderer pdfRenderer, CurrentUser currentUser) {
        this.reportService = reportService;
        this.pdfRenderer = pdfRenderer;
        this.currentUser = currentUser;
    }

    @GET
    @Path("/history")
    public List<ReportHistoryResponse> history() {
        return reportService.history(currentUser.id());
    }

    @GET
    @Path("/{type}")
    @Produces({MediaType.APPLICATION_JSON, "application/pdf"})
    public Response get(@PathParam("type") String type,
                        @QueryParam("format") String format,
                        @QueryParam("includeSensitive") boolean includeSensitive,
                        @QueryParam("sections") String sections) {
        ReportType reportType = ReportType.fromString(type);
        ReportFormat reportFormat = ReportFormat.fromString(format);
        ReportOptions options = new ReportOptions(includeSensitive, parseSections(sections));

        ReportDocument doc = reportService.generate(currentUser.id(), reportType, reportFormat, options);

        if (reportFormat == ReportFormat.PDF) {
            byte[] pdf = pdfRenderer.render(doc);
            String filename = reportType.name().toLowerCase() + ".pdf";
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .build();
        }
        return Response.ok(doc).build();
    }

    private static Set<String> parseSections(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }
}
