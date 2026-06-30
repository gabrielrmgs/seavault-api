package br.dev.irontech.seavault.vessels.service;

import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.vessels.domain.Vessel;
import br.dev.irontech.seavault.vessels.dto.VesselRequest;
import br.dev.irontech.seavault.vessels.dto.VesselResponse;
import br.dev.irontech.seavault.vessels.repo.VesselRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class VesselService {

    private final VesselRepository vesselRepository;
    private final ReferenceRepository referenceRepository;

    public VesselService(VesselRepository vesselRepository, ReferenceRepository referenceRepository) {
        this.vesselRepository = vesselRepository;
        this.referenceRepository = referenceRepository;
    }

    @Transactional
    public VesselResponse create(UUID userId, VesselRequest req) {
        validateType(req.typeId());
        Vessel v = new Vessel();
        v.userId = userId;
        apply(v, req);
        vesselRepository.persist(v);
        return toResponse(v);
    }

    public VesselResponse get(UUID userId, UUID id) {
        return toResponse(requireOwned(userId, id));
    }

    public PageResponse<VesselResponse> list(UUID userId, PageRequest page) {
        List<VesselResponse> content = vesselRepository.listActiveByUser(userId, page).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(content, page, vesselRepository.countActiveByUser(userId));
    }

    public List<VesselResponse> listAllForUser(UUID userId) {
        return vesselRepository.listAllActiveByUser(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VesselResponse update(UUID userId, UUID id, VesselRequest req) {
        Vessel v = requireOwned(userId, id);
        validateType(req.typeId());
        apply(v, req);
        return toResponse(v);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Vessel v = requireOwned(userId, id);
        v.deletedAt = Instant.now();
    }

    private void apply(Vessel v, VesselRequest req) {
        v.name = req.name();
        v.typeId = req.typeId();
        v.imo = req.imo();
        v.flag = req.flag();
        v.grossTonnage = req.grossTonnage();
        v.notes = req.notes();
    }

    private void validateType(UUID typeId) {
        if (typeId != null
                && referenceRepository.findTypeById(typeId).filter(type -> "VESSEL".equals(type.kind)).isEmpty()) {
            throw new NotFoundException("Tipo de embarcacao nao encontrado: " + typeId);
        }
    }

    private Vessel requireOwned(UUID userId, UUID id) {
        return vesselRepository.findActiveByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Embarcacao nao encontrada: " + id));
    }

    private VesselResponse toResponse(Vessel v) {
        return new VesselResponse(v.id, v.name, v.typeId, v.imo, v.flag,
                v.grossTonnage, v.notes, v.createdAt, v.updatedAt);
    }
}
