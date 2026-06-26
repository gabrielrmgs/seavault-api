package br.dev.irontech.seavault.documents.service;

import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.scan.DueItem;
import br.dev.irontech.seavault.common.expiry.ExpiryStatus;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.documents.domain.Document;
import br.dev.irontech.seavault.documents.dto.DocumentRequest;
import br.dev.irontech.seavault.documents.dto.DocumentResponse;
import br.dev.irontech.seavault.documents.repo.DocumentRepository;
import br.dev.irontech.seavault.files.domain.OwnerType;
import br.dev.irontech.seavault.files.dto.FileResponse;
import br.dev.irontech.seavault.files.service.FileService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ReferenceRepository referenceRepository;
    private final FileService fileService;

    @ConfigProperty(name = "seavault.expiry.warning-days")
    int warningDays;

    public DocumentService(DocumentRepository documentRepository,
                           ReferenceRepository referenceRepository,
                           FileService fileService) {
        this.documentRepository = documentRepository;
        this.referenceRepository = referenceRepository;
        this.fileService = fileService;
    }

    @Transactional
    public DocumentResponse create(UUID userId, DocumentRequest req) {
        validateType(req.typeId());
        Document d = new Document();
        d.userId = userId;
        apply(d, req);
        documentRepository.persist(d);
        return toResponse(d);
    }

    public DocumentResponse get(UUID userId, UUID id) {
        return toResponse(requireOwned(userId, id));
    }

    public PageResponse<DocumentResponse> list(UUID userId, PageRequest page) {
        List<DocumentResponse> content = documentRepository.listActiveByUser(userId, page).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(content, page, documentRepository.countActiveByUser(userId));
    }

    @Transactional
    public DocumentResponse update(UUID userId, UUID id, DocumentRequest req) {
        Document d = requireOwned(userId, id);
        validateType(req.typeId());
        apply(d, req);
        return toResponse(d);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Document d = requireOwned(userId, id);
        d.deletedAt = Instant.now();
        fileService.unlinkAll(OwnerType.DOCUMENT, id);
    }

    @Transactional
    public void attachFile(UUID userId, UUID id, UUID fileId) {
        requireOwned(userId, id);
        fileService.link(userId, fileId, OwnerType.DOCUMENT, id);
    }

    @Transactional
    public void detachFile(UUID userId, UUID id, UUID fileId) {
        requireOwned(userId, id);
        fileService.unlink(userId, fileId, OwnerType.DOCUMENT, id);
    }

    public List<FileResponse> listFiles(UUID userId, UUID id) {
        requireOwned(userId, id);
        return fileService.filesForOwner(userId, OwnerType.DOCUMENT, id);
    }

    public List<DueItem> dueForAlerts(LocalDate maxDate) {
        return documentRepository.listExpiringAllUsers(maxDate).stream()
                .map(d -> new DueItem(d.userId, d.id, d.expiryDate,
                        "Vencimento de documento" + (d.number != null ? " " + d.number : "")))
                .toList();
    }

    public List<DocumentResponse> listAllForUser(UUID userId) {
        return documentRepository.listAllActiveByUser(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void apply(Document d, DocumentRequest req) {
        d.typeId = req.typeId();
        d.number = req.number();
        d.issuer = req.issuer();
        d.issueDate = req.issueDate();
        d.expiryDate = req.expiryDate();
        d.notes = req.notes();
    }

    private void validateType(UUID typeId) {
        if (referenceRepository.findTypeById(typeId).filter(type -> "DOCUMENT".equals(type.kind)).isEmpty()) {
            throw new NotFoundException("Tipo de documento nao encontrado: " + typeId);
        }
    }

    private Document requireOwned(UUID userId, UUID id) {
        return documentRepository.findActiveByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Documento nao encontrado: " + id));
    }

    private DocumentResponse toResponse(Document d) {
        ExpiryStatus status = ExpiryStatus.of(d.expiryDate, LocalDate.now(), warningDays);
        return new DocumentResponse(d.id, d.typeId, d.number, d.issuer, d.issueDate,
                d.expiryDate, status, d.notes, d.createdAt, d.updatedAt);
    }
}
