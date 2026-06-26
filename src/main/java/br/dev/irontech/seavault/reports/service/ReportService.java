package br.dev.irontech.seavault.reports.service;

import br.dev.irontech.seavault.certificates.dto.CertificateResponse;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.courses.dto.CourseResponse;
import br.dev.irontech.seavault.courses.service.CourseService;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.companies.service.CompanyService;
import br.dev.irontech.seavault.documents.dto.DocumentResponse;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.profile.dto.ProfileResponse;
import br.dev.irontech.seavault.profile.service.ProfileService;
import br.dev.irontech.seavault.auth.repo.UserRepository;
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
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse;
import br.dev.irontech.seavault.seatime.service.SeatimeService;
import br.dev.irontech.seavault.vessels.service.VesselService;
import br.dev.irontech.seavault.voyages.dto.VoyageResponse;
import br.dev.irontech.seavault.voyages.service.VoyageService;
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
    private final SeatimeService seatimeService;
    private final VoyageService voyageService;
    private final VesselService vesselService;
    private final CompanyService companyService;
    private final ProfileService profileService;
    private final UserRepository userRepository;
    private final CourseService courseService;

    public ReportService(DocumentService documentService,
                         CertificateService certificateService,
                         ReferenceRepository referenceRepository,
                         ReportHistoryRepository reportHistoryRepository,
                         SeatimeService seatimeService,
                         VoyageService voyageService,
                         VesselService vesselService,
                         CompanyService companyService,
                         ProfileService profileService,
                         UserRepository userRepository,
                         CourseService courseService) {
        this.documentService = documentService;
        this.certificateService = certificateService;
        this.referenceRepository = referenceRepository;
        this.reportHistoryRepository = reportHistoryRepository;
        this.seatimeService = seatimeService;
        this.voyageService = voyageService;
        this.vesselService = vesselService;
        this.companyService = companyService;
        this.profileService = profileService;
        this.userRepository = userRepository;
        this.courseService = courseService;
    }

    @Transactional
    public ReportDocument generate(UUID userId, ReportType type, ReportFormat format, ReportOptions options) {
        Instant now = Instant.now();
        ReportDocument doc = switch (type) {
            case DOCUMENTS -> buildDocuments(userId, options, now);
            case CERTIFICATES -> buildCertificates(userId, options, now);
            case SEATIME -> buildSeatime(userId, options, now);
            case CAREER -> buildCareer(userId, options, now);
            case CV -> buildCv(userId, options, now);
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

    private ReportDocument buildSeatime(UUID userId, ReportOptions options, Instant now) {
        SeatimeSummaryResponse st = seatimeService.summary(userId);
        List<Section> sections = new ArrayList<>();

        if (options.wants("totals")) {
            sections.add(new Section("Totais", List.of(
                    new Field("Total de dias", str(st.totalDays())),
                    new Field("Dias (embarque ativo)", str(st.activeDays())),
                    new Field("Últimos 12 meses", str(st.last12MonthsDays())),
                    new Field("Últimos 5 anos", str(st.last5YearsDays())),
                    new Field("Maior contrato (dias)",
                            st.longestContractDays() == null ? "—" : str(st.longestContractDays()))),
                    null));
        }
        if (options.wants("byCompany")) {
            List<List<String>> rows = st.byCompany().stream()
                    .map(e -> List.of(companyName(userId, e.id()), str(e.days())))
                    .toList();
            sections.add(new Section("Por empresa", List.of(),
                    new Table(List.of("Empresa", "Dias"), rows)));
        }
        if (options.wants("byVessel")) {
            List<List<String>> rows = st.byVessel().stream()
                    .map(e -> List.of(vesselName(userId, e.id()), str(e.days())))
                    .toList();
            sections.add(new Section("Por embarcação", List.of(),
                    new Table(List.of("Embarcação", "Dias"), rows)));
        }
        if (options.wants("byYear")) {
            List<List<String>> rows = st.byYear().stream()
                    .map(e -> List.of(str(e.year()), str(e.days())))
                    .toList();
            sections.add(new Section("Por ano", List.of(),
                    new Table(List.of("Ano", "Dias"), rows)));
        }
        return new ReportDocument("SEATIME", "Relatório de Tempo de Embarque", now, sections);
    }

    private ReportDocument buildCareer(UUID userId, ReportOptions options, Instant now) {
        List<Section> sections = new ArrayList<>();

        if (options.wants("profile")) {
            ProfileResponse p = profileService.getOrCreate(userId);
            String userName = userRepository.findByIdOptional(userId).map(u -> u.name).orElse("—");
            List<Field> fields = new ArrayList<>();
            fields.add(new Field("Nome", nz(userName)));
            fields.add(new Field("CIR", nz(p.cir())));
            fields.add(new Field("Nacionalidade", nz(p.nationality())));
            fields.add(new Field("Categoria", categoryName(p.categoryId())));
            if (options.includeSensitive()) {
                fields.add(new Field("CPF", nz(p.cpf())));
                fields.add(new Field("RG", nz(p.rg())));
            }
            sections.add(new Section("Perfil", fields, null));
        }

        if (options.wants("seatime")) {
            SeatimeSummaryResponse st = seatimeService.summary(userId);
            sections.add(new Section("Tempo de mar", List.of(
                    new Field("Total de dias", str(st.totalDays())),
                    new Field("Últimos 12 meses", str(st.last12MonthsDays()))),
                    null));
        }

        if (options.wants("voyages")) {
            List<VoyageResponse> voyages = voyageService.listAllForUser(userId);
            List<List<String>> rows = voyages.stream()
                    .map(v -> List.of(
                            vesselName(userId, v.vesselId()),
                            companyName(userId, v.companyId()),
                            nz(v.role()),
                            dateStr(v.embarkDate()),
                            dateStr(v.disembarkDate()),
                            v.effectiveDays() == null ? "—" : str(v.effectiveDays()),
                            nz(v.status())))
                    .toList();
            sections.add(new Section("Embarques", List.of(),
                    new Table(List.of("Embarcação", "Empresa", "Função", "Embarque", "Desembarque", "Dias", "Status"),
                            rows)));
        }

        return new ReportDocument("CAREER", "Relatório de Carreira", now, sections);
    }

    private ReportDocument buildCv(UUID userId, ReportOptions options, Instant now) {
        List<Section> sections = new ArrayList<>();

        if (options.wants("profile")) {
            ProfileResponse p = profileService.getOrCreate(userId);
            String userName = userRepository.findByIdOptional(userId).map(u -> u.name).orElse("—");
            List<Field> fields = new ArrayList<>();
            fields.add(new Field("Nome", nz(userName)));
            fields.add(new Field("Nacionalidade", nz(p.nationality())));
            fields.add(new Field("Categoria", categoryName(p.categoryId())));
            fields.add(new Field("CIR", nz(p.cir())));
            if (options.includeSensitive()) {
                fields.add(new Field("CPF", nz(p.cpf())));
                fields.add(new Field("RG", nz(p.rg())));
            }
            sections.add(new Section("Perfil", fields, null));
        }

        if (options.wants("certificates")) {
            List<CertificateResponse> certs = certificateService.listAllForUser(userId);
            List<List<String>> rows = certs.stream()
                    .map(c -> List.of(nz(c.name()), nz(c.institution()), dateStr(c.expiryDate()), c.status().name()))
                    .toList();
            sections.add(new Section("Certificados", List.of(),
                    new Table(List.of("Nome", "Instituição", "Validade", "Status"), rows)));
        }

        if (options.wants("courses")) {
            List<CourseResponse> courses = courseService.listAllForUser(userId);
            List<List<String>> rows = courses.stream()
                    .map(c -> List.of(
                            nz(c.name()),
                            nz(c.institution()),
                            c.workloadHours() == null ? "—" : str(c.workloadHours()),
                            c.status().name()))
                    .toList();
            sections.add(new Section("Cursos", List.of(),
                    new Table(List.of("Curso", "Instituição", "Carga horária", "Status"), rows)));
        }

        if (options.wants("seatime")) {
            SeatimeSummaryResponse st = seatimeService.summary(userId);
            sections.add(new Section("Tempo de mar", List.of(
                    new Field("Total de dias", str(st.totalDays())),
                    new Field("Maior contrato (dias)",
                            st.longestContractDays() == null ? "—" : str(st.longestContractDays()))),
                    null));
        }

        return new ReportDocument("CV", "Currículo Marítimo", now, sections);
    }

    String vesselName(UUID userId, UUID id) {
        if (id == null) {
            return "—";
        }
        try {
            return vesselService.get(userId, id).name();
        } catch (NotFoundException e) {
            return "—";
        }
    }

    String companyName(UUID userId, UUID id) {
        if (id == null) {
            return "—";
        }
        try {
            return companyService.get(userId, id).name();
        } catch (NotFoundException e) {
            return "—";
        }
    }

    String categoryName(UUID id) {
        if (id == null) {
            return "—";
        }
        return referenceRepository.findCategoryById(id).map(c -> c.name).orElse("—");
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
