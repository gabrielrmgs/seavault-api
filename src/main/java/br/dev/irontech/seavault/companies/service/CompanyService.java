package br.dev.irontech.seavault.companies.service;

import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.companies.domain.Company;
import br.dev.irontech.seavault.companies.dto.CompanyRequest;
import br.dev.irontech.seavault.companies.dto.CompanyResponse;
import br.dev.irontech.seavault.companies.repo.CompanyRepository;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ReferenceRepository referenceRepository;

    public CompanyService(CompanyRepository companyRepository, ReferenceRepository referenceRepository) {
        this.companyRepository = companyRepository;
        this.referenceRepository = referenceRepository;
    }

    @Transactional
    public CompanyResponse create(UUID userId, CompanyRequest req) {
        validateType(req.typeId());
        Company c = new Company();
        c.userId = userId;
        apply(c, req);
        companyRepository.persist(c);
        return toResponse(c);
    }

    public CompanyResponse get(UUID userId, UUID id) {
        return toResponse(requireOwned(userId, id));
    }

    public PageResponse<CompanyResponse> list(UUID userId, PageRequest page) {
        List<CompanyResponse> content = companyRepository.listActiveByUser(userId, page).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(content, page, companyRepository.countActiveByUser(userId));
    }

    public List<CompanyResponse> listAllForUser(UUID userId) {
        return companyRepository.listAllActiveByUser(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CompanyResponse update(UUID userId, UUID id, CompanyRequest req) {
        Company c = requireOwned(userId, id);
        validateType(req.typeId());
        apply(c, req);
        return toResponse(c);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Company c = requireOwned(userId, id);
        c.deletedAt = Instant.now();
    }

    private void apply(Company c, CompanyRequest req) {
        c.name = req.name();
        c.typeId = req.typeId();
        c.cnpj = req.cnpj();
        c.email = req.email();
        c.phone = req.phone();
        c.notes = req.notes();
    }

    private void validateType(UUID typeId) {
        if (typeId != null
                && referenceRepository.findTypeById(typeId).filter(type -> "COMPANY".equals(type.kind)).isEmpty()) {
            throw new NotFoundException("Tipo de empresa nao encontrado: " + typeId);
        }
    }

    private Company requireOwned(UUID userId, UUID id) {
        return companyRepository.findActiveByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Empresa nao encontrada: " + id));
    }

    private CompanyResponse toResponse(Company c) {
        return new CompanyResponse(c.id, c.name, c.typeId, c.cnpj, c.email, c.phone,
                c.notes, c.createdAt, c.updatedAt);
    }
}
