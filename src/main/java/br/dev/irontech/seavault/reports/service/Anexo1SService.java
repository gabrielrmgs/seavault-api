package br.dev.irontech.seavault.reports.service;

import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.FieldError;
import br.dev.irontech.seavault.profile.dto.ProfileResponse;
import br.dev.irontech.seavault.profile.service.ProfileService;
import br.dev.irontech.seavault.reports.domain.ReportFormat;
import br.dev.irontech.seavault.reports.domain.ReportRecord;
import br.dev.irontech.seavault.reports.domain.ReportType;
import br.dev.irontech.seavault.reports.dto.Anexo1SRequest;
import br.dev.irontech.seavault.reports.pdf.ReportDocument;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Field;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Section;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Table;
import br.dev.irontech.seavault.reports.repo.ReportHistoryRepository;
import br.dev.irontech.seavault.voyages.dto.VoyageResponse;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class Anexo1SService {

    private final ProfileService profileService;
    private final UserRepository userRepository;
    private final VoyageService voyageService;
    private final ReportHistoryRepository reportHistoryRepository;
    private final ReportNames names;

    public Anexo1SService(ProfileService profileService,
                          UserRepository userRepository,
                          VoyageService voyageService,
                          ReportHistoryRepository reportHistoryRepository,
                          ReportNames names) {
        this.profileService = profileService;
        this.userRepository = userRepository;
        this.voyageService = voyageService;
        this.reportHistoryRepository = reportHistoryRepository;
        this.names = names;
    }

    @Transactional
    public ReportDocument generate(UUID userId, Anexo1SRequest req, ReportFormat format) {
        Instant now = Instant.now();
        ProfileResponse profile = profileService.getOrCreate(userId);

        List<FieldError> errors = new ArrayList<>();
        if (isBlank(profile.cpf())) {
            errors.add(new FieldError("cpf", "CPF é obrigatório para o Anexo 1-S"));
        }
        if (isBlank(profile.cir())) {
            errors.add(new FieldError("cir", "CIR é obrigatório para o Anexo 1-S"));
        }

        List<UUID> voyageIds = req == null || req.voyageIds() == null ? List.of() : req.voyageIds();
        if (voyageIds.isEmpty()) {
            errors.add(new FieldError("voyageIds", "Selecione ao menos um embarque"));
        }

        List<VoyageResponse> voyages = new ArrayList<>();
        for (UUID id : voyageIds) {
            VoyageResponse voyage = voyageService.get(userId, id);
            voyages.add(voyage);
            String prefix = "voyages[" + id + "].";
            if (!"FINISHED".equals(voyage.status())) {
                errors.add(new FieldError(prefix + "disembarkDate",
                        "Embarque sem data de desembarque não pode ser atestado"));
            }
            if (voyage.vesselId() == null) {
                errors.add(new FieldError(prefix + "vesselId", "Embarcação é obrigatória"));
            }
            if (voyage.companyId() == null) {
                errors.add(new FieldError(prefix + "companyId", "Empresa é obrigatória"));
            }
            if (isBlank(voyage.role())) {
                errors.add(new FieldError(prefix + "role", "Função é obrigatória"));
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException("Anexo 1-S incompleto: preencha os campos obrigatórios", errors);
        }

        ReportDocument document = build(userId, profile, voyages, now);
        recordHistory(userId, format, voyageIds, now);
        return document;
    }

    private ReportDocument build(UUID userId, ProfileResponse profile, List<VoyageResponse> voyages, Instant now) {
        String userName = userRepository.findByIdOptional(userId).map(u -> u.name).orElse("—");
        List<Section> sections = new ArrayList<>();

        sections.add(new Section("Identificação do Aquaviário", List.of(
                new Field("Nome", ReportNames.nz(userName)),
                new Field("CPF", ReportNames.nz(profile.cpf())),
                new Field("CIR", ReportNames.nz(profile.cir())),
                new Field("Nacionalidade", ReportNames.nz(profile.nationality())),
                new Field("Categoria", names.categoryName(profile.categoryId()))),
                null));

        List<List<String>> rows = new ArrayList<>();
        long totalDays = 0L;
        for (VoyageResponse voyage : voyages) {
            long days = voyage.effectiveDays() == null ? 0L : voyage.effectiveDays();
            totalDays += days;
            rows.add(List.of(
                    names.vesselName(userId, voyage.vesselId()),
                    names.companyName(userId, voyage.companyId()),
                    ReportNames.nz(voyage.role()),
                    ReportNames.dateStr(voyage.embarkDate()),
                    ReportNames.nz(voyage.embarkPort()),
                    ReportNames.dateStr(voyage.disembarkDate()),
                    ReportNames.nz(voyage.disembarkPort()),
                    ReportNames.str(days)));
        }
        sections.add(new Section("Embarques Atestados", List.of(),
                new Table(List.of("Embarcação", "Empresa", "Função", "Embarque", "Porto Emb.",
                        "Desembarque", "Porto Des.", "Dias"), rows)));

        sections.add(new Section("Resumo", List.of(
                new Field("Total de embarques", ReportNames.str(voyages.size())),
                new Field("Total de dias atestados", ReportNames.str(totalDays))),
                null));

        return new ReportDocument("ANEXO_1S",
                "Atestado de Embarque de Aquaviário — Anexo 1-S", now, sections);
    }

    private void recordHistory(UUID userId, ReportFormat format, List<UUID> voyageIds, Instant now) {
        ReportRecord record = new ReportRecord();
        record.userId = userId;
        record.type = ReportType.ANEXO_1S;
        record.format = format;
        record.params = "voyageIds=" + voyageIds.stream().map(UUID::toString).collect(Collectors.joining(","));
        record.generatedAt = now;
        reportHistoryRepository.persist(record);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
