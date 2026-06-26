package br.dev.irontech.seavault.voyages.service;

import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.scan.DueItem;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.companies.service.CompanyService;
import br.dev.irontech.seavault.files.domain.OwnerType;
import br.dev.irontech.seavault.files.dto.FileResponse;
import br.dev.irontech.seavault.files.service.FileService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.vessels.service.VesselService;
import br.dev.irontech.seavault.voyages.domain.Voyage;
import br.dev.irontech.seavault.voyages.domain.VoyageStatus;
import br.dev.irontech.seavault.voyages.dto.VoyageRequest;
import br.dev.irontech.seavault.voyages.dto.VoyageResponse;
import br.dev.irontech.seavault.voyages.repo.VoyageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class VoyageService {

    private final VoyageRepository voyageRepository;
    private final VesselService vesselService;
    private final CompanyService companyService;
    private final ReferenceRepository referenceRepository;
    private final FileService fileService;

    public VoyageService(VoyageRepository voyageRepository,
                         VesselService vesselService,
                         CompanyService companyService,
                         ReferenceRepository referenceRepository,
                         FileService fileService) {
        this.voyageRepository = voyageRepository;
        this.vesselService = vesselService;
        this.companyService = companyService;
        this.referenceRepository = referenceRepository;
        this.fileService = fileService;
    }

    @Transactional
    public VoyageResponse create(UUID userId, VoyageRequest req) {
        validate(userId, req);
        Voyage v = new Voyage();
        v.userId = userId;
        apply(v, req);
        voyageRepository.persist(v);
        return toResponse(v);
    }

    public VoyageResponse get(UUID userId, UUID id) {
        return toResponse(requireOwned(userId, id));
    }

    public PageResponse<VoyageResponse> list(UUID userId, PageRequest page) {
        List<VoyageResponse> content = voyageRepository.listActiveByUser(userId, page).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(content, page, voyageRepository.countActiveByUser(userId));
    }

    public List<VoyageResponse> listAllForUser(UUID userId) {
        return voyageRepository.listAllActiveByUser(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DueItem> dueForAlerts(LocalDate cutoff) {
        return voyageRepository.listActiveEmbarkedBeforeAllUsers(cutoff).stream()
                .map(v -> new DueItem(v.userId, v.id, v.embarkDate, "Embarque ativo desde " + v.embarkDate))
                .toList();
    }

    @Transactional
    public VoyageResponse update(UUID userId, UUID id, VoyageRequest req) {
        Voyage v = requireOwned(userId, id);
        validate(userId, req);
        apply(v, req);
        return toResponse(v);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Voyage v = requireOwned(userId, id);
        v.deletedAt = Instant.now();
        fileService.unlinkAll(OwnerType.VOYAGE, id);
    }

    @Transactional
    public void attachFile(UUID userId, UUID id, UUID fileId) {
        requireOwned(userId, id);
        fileService.link(userId, fileId, OwnerType.VOYAGE, id);
    }

    @Transactional
    public void detachFile(UUID userId, UUID id, UUID fileId) {
        requireOwned(userId, id);
        fileService.unlink(userId, fileId, OwnerType.VOYAGE, id);
    }

    public List<FileResponse> listFiles(UUID userId, UUID id) {
        requireOwned(userId, id);
        return fileService.filesForOwner(userId, OwnerType.VOYAGE, id);
    }

    private void validate(UUID userId, VoyageRequest req) {
        if (req.disembarkDate() != null && req.disembarkDate().isBefore(req.embarkDate())) {
            throw new BusinessException("Data de desembarque anterior ao embarque");
        }
        if (req.vesselId() != null) {
            vesselService.get(userId, req.vesselId());
        }
        if (req.companyId() != null) {
            companyService.get(userId, req.companyId());
        }
        if (req.navigationTypeId() != null
                && referenceRepository.findTypeById(req.navigationTypeId())
                        .filter(t -> "NAVIGATION".equals(t.kind)).isEmpty()) {
            throw new NotFoundException("Tipo de navegacao nao encontrado: " + req.navigationTypeId());
        }
        if (req.categoryId() != null && referenceRepository.findCategoryById(req.categoryId()).isEmpty()) {
            throw new NotFoundException("Categoria nao encontrada: " + req.categoryId());
        }
    }

    private void apply(Voyage v, VoyageRequest req) {
        v.embarkDate = req.embarkDate();
        v.disembarkDate = req.disembarkDate();
        v.vesselId = req.vesselId();
        v.companyId = req.companyId();
        v.navigationTypeId = req.navigationTypeId();
        v.categoryId = req.categoryId();
        v.role = req.role();
        v.embarkPort = req.embarkPort();
        v.disembarkPort = req.disembarkPort();
        v.notes = req.notes();
        v.calculatedDays = req.disembarkDate() == null ? null : inclusiveDays(req.embarkDate(), req.disembarkDate());
        if (req.computedDays() != null) {
            v.computedDays = req.computedDays();
            v.overrideReason = req.overrideReason();
            v.overriddenAt = Instant.now();
        } else {
            v.computedDays = null;
            v.overrideReason = null;
            v.overriddenAt = null;
        }
    }

    private Voyage requireOwned(UUID userId, UUID id) {
        return voyageRepository.findActiveByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Embarque nao encontrado: " + id));
    }

    private static int inclusiveDays(LocalDate embark, LocalDate disembark) {
        return (int) ChronoUnit.DAYS.between(embark, disembark) + 1;
    }

    private VoyageResponse toResponse(Voyage v) {
        boolean finished = v.disembarkDate != null;
        String status = (finished ? VoyageStatus.FINISHED : VoyageStatus.ACTIVE).name();
        Integer effectiveDays;
        if (finished) {
            effectiveDays = v.computedDays != null ? v.computedDays : v.calculatedDays;
        } else {
            effectiveDays = inclusiveDays(v.embarkDate, LocalDate.now());
        }
        return new VoyageResponse(v.id, v.vesselId, v.companyId, v.navigationTypeId, v.categoryId,
                v.role, v.embarkDate, v.disembarkDate, v.embarkPort, v.disembarkPort,
                status, v.calculatedDays, v.computedDays, effectiveDays,
                v.overrideReason, v.overriddenAt, v.notes, v.createdAt, v.updatedAt);
    }
}
