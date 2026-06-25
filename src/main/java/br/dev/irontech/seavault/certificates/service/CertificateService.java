package br.dev.irontech.seavault.certificates.service;

import br.dev.irontech.seavault.certificates.domain.Certificate;
import br.dev.irontech.seavault.certificates.dto.CertificateRequest;
import br.dev.irontech.seavault.certificates.dto.CertificateResponse;
import br.dev.irontech.seavault.certificates.repo.CertificateRepository;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.expiry.ExpiryStatus;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.files.domain.OwnerType;
import br.dev.irontech.seavault.files.dto.FileResponse;
import br.dev.irontech.seavault.files.service.FileService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final FileService fileService;

    @ConfigProperty(name = "seavault.expiry.warning-days")
    int warningDays;

    public CertificateService(CertificateRepository certificateRepository, FileService fileService) {
        this.certificateRepository = certificateRepository;
        this.fileService = fileService;
    }

    @Transactional
    public CertificateResponse create(UUID userId, CertificateRequest req) {
        Certificate c = new Certificate();
        c.userId = userId;
        apply(c, req);
        certificateRepository.persist(c);
        return toResponse(c);
    }

    public CertificateResponse get(UUID userId, UUID id) {
        return toResponse(requireOwned(userId, id));
    }

    public PageResponse<CertificateResponse> list(UUID userId, PageRequest page) {
        List<CertificateResponse> content = certificateRepository.listActiveByUser(userId, page).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(content, page, certificateRepository.countActiveByUser(userId));
    }

    @Transactional
    public CertificateResponse update(UUID userId, UUID id, CertificateRequest req) {
        Certificate c = requireOwned(userId, id);
        apply(c, req);
        return toResponse(c);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Certificate c = requireOwned(userId, id);
        c.deletedAt = Instant.now();
        fileService.unlinkAll(OwnerType.CERTIFICATE, id);
    }

    @Transactional
    public void attachFile(UUID userId, UUID id, UUID fileId) {
        requireOwned(userId, id);
        fileService.link(userId, fileId, OwnerType.CERTIFICATE, id);
    }

    @Transactional
    public void detachFile(UUID userId, UUID id, UUID fileId) {
        requireOwned(userId, id);
        fileService.unlink(userId, fileId, OwnerType.CERTIFICATE, id);
    }

    public List<FileResponse> listFiles(UUID userId, UUID id) {
        requireOwned(userId, id);
        return fileService.filesForOwner(userId, OwnerType.CERTIFICATE, id);
    }

    private void apply(Certificate c, CertificateRequest req) {
        c.name = req.name();
        c.code = req.code();
        c.institution = req.institution();
        c.issueDate = req.issueDate();
        c.expiryDate = req.expiryDate();
        c.notes = req.notes();
    }

    private Certificate requireOwned(UUID userId, UUID id) {
        return certificateRepository.findActiveByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Certificado nao encontrado: " + id));
    }

    private CertificateResponse toResponse(Certificate c) {
        ExpiryStatus status = ExpiryStatus.of(c.expiryDate, LocalDate.now(), warningDays);
        return new CertificateResponse(c.id, c.name, c.code, c.institution, c.issueDate,
                c.expiryDate, status, c.notes, c.createdAt, c.updatedAt);
    }
}
