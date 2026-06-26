package br.dev.irontech.seavault.reports.service;

import br.dev.irontech.seavault.certificates.dto.CertificateResponse;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.documents.dto.DocumentResponse;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.reports.domain.ReportFormat;
import br.dev.irontech.seavault.reports.domain.ReportRecord;
import br.dev.irontech.seavault.reports.domain.ReportType;
import br.dev.irontech.seavault.reports.dto.ReportHistoryResponse;
import br.dev.irontech.seavault.reports.dto.ReportOptions;
import br.dev.irontech.seavault.reports.pdf.ReportDocument;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Field;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Section;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Table;
import br.dev.irontech.seavault.reports.repo.ReportHistoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ReportService {

    private final DocumentService documentService;
    private final CertificateService certificateService;
    private final ReferenceRepository referenceRepository;
    private final ReportHistoryRepository reportHistoryRepository;

    public ReportService(DocumentService documentService,
                         CertificateService certificateService,
                         ReferenceRepository referenceRepository,
                         ReportHistoryRepository reportHistoryRepository) {
        this.documentService = documentService;
        this.certificateService = certificateService;
        this.referenceRepository = referenceRepository;
        this.reportHistoryRepository = reportHistoryRepository;
    }

    @Transactional
    public ReportDocument generate(UUID userId, ReportType type, ReportFormat format, ReportOptions options) {
        Instant now = Instant.now();
        ReportDocument doc = switch (type) {
            case DOCUMENTS -> buildDocuments(userId, options, now);
            case CERTIFICATES -> buildCertificates(userId, options, now);
            case SEATIME -> throw new UnsupportedOperationException("SEATIME implementado na Task 4");
            case CAREER -> throw new UnsupportedOperationException("CAREER implementado na Task 4");
            case CV -> throw new UnsupportedOperationException("CV implementado na Task 5");
        };
        recordHistory(userId, type, format, options, now);
        return doc;
    }

    public List<ReportHistoryResponse> history(UUID userId) {
        return reportHistoryRepository.listByUser(userId).stream()
                .map(r -> new ReportHistoryResponse(r.id, r.type, r.format, r.params, r.generatedAt))
                .toList();
    }

    private ReportDocument buildDocuments(UUID userId, ReportOptions options, Instant now) {
        List<DocumentResponse> docs = documentService.listAllForUser(userId);
        List<List<String>> rows = docs.stream()
                .map(d -> List.of(
                        typeLabel(d.typeId()),
                        nz(d.number()),
                        nz(d.issuer()),
                        dateStr(d.expiryDate()),
                        d.status().name()))
                .toList();
        Section section = new Section(
                "Documentos",
                List.of(new Field("Total", str(docs.size()))),
                new Table(List.of("Tipo", "Número", "Emissor", "Validade", "Status"), rows));
        return new ReportDocument("DOCUMENTS", "Relatório de Documentos", now, List.of(section));
    }

    private ReportDocument buildCertificates(UUID userId, ReportOptions options, Instant now) {
        List<CertificateResponse> certs = certificateService.listAllForUser(userId);
        List<List<String>> rows = certs.stream()
                .map(c -> List.of(
                        nz(c.name()),
                        nz(c.code()),
                        nz(c.institution()),
                        dateStr(c.expiryDate()),
                        c.status().name()))
                .toList();
        Section section = new Section(
                "Certificados",
                List.of(new Field("Total", str(certs.size()))),
                new Table(List.of("Nome", "Código", "Instituição", "Validade", "Status"), rows));
        return new ReportDocument("CERTIFICATES", "Relatório de Certificados", now, List.of(section));
    }

    private void recordHistory(UUID userId, ReportType type, ReportFormat format, ReportOptions options, Instant now) {
        ReportRecord r = new ReportRecord();
        r.userId = userId;
        r.type = type;
        r.format = format;
        r.params = "includeSensitive=" + options.includeSensitive()
                + ";sections=" + String.join(",", options.sections());
        r.generatedAt = now;
        reportHistoryRepository.persist(r);
    }

    // ---- helpers reusados pelas Tasks 4 e 5 ----

    String typeLabel(UUID typeId) {
        if (typeId == null) {
            return "—";
        }
        return referenceRepository.findTypeById(typeId).map(t -> t.label).orElse("—");
    }

    static String nz(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    static String dateStr(LocalDate d) {
        return d == null ? "—" : d.toString();
    }

    static String str(long n) {
        return String.valueOf(n);
    }
}
