package br.dev.irontech.seavault.files.service;

import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.files.domain.FileLink;
import br.dev.irontech.seavault.files.domain.StoredFile;
import br.dev.irontech.seavault.files.dto.FileDownload;
import br.dev.irontech.seavault.files.dto.FileResponse;
import br.dev.irontech.seavault.files.repo.FileLinkRepository;
import br.dev.irontech.seavault.files.repo.FileRepository;
import br.dev.irontech.seavault.files.storage.FileStorage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class FileService {

    private final FileRepository fileRepository;
    private final FileLinkRepository fileLinkRepository;
    private final FileStorage storage;

    @ConfigProperty(name = "seavault.files.max-size-bytes")
    long maxSizeBytes;

    @ConfigProperty(name = "seavault.files.allowed-content-types")
    String allowedContentTypesCsv;

    public FileService(FileRepository fileRepository,
                       FileLinkRepository fileLinkRepository,
                       FileStorage storage) {
        this.fileRepository = fileRepository;
        this.fileLinkRepository = fileLinkRepository;
        this.storage = storage;
    }

    @Transactional
    public FileResponse upload(UUID userId, String originalName, String contentType, byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessException("Arquivo vazio");
        }
        if (content.length > maxSizeBytes) {
            throw new BusinessException("Arquivo excede o tamanho maximo de " + maxSizeBytes + " bytes");
        }
        String ct = contentType == null ? "" : contentType.trim().toLowerCase();
        if (!allowedContentTypes().contains(ct)) {
            throw new BusinessException("Tipo de arquivo nao permitido: " + ct);
        }

        String storageKey = userId + "/" + UUID.randomUUID();
        storage.store(storageKey, content);

        StoredFile f = new StoredFile();
        f.userId = userId;
        f.originalName = sanitizeName(originalName);
        f.contentType = ct;
        f.sizeBytes = content.length;
        f.storageKey = storageKey;
        f.sha256 = sha256(content);
        fileRepository.persist(f);

        return toResponse(f);
    }

    public FileDownload download(UUID userId, UUID fileId) {
        StoredFile f = requireOwned(userId, fileId);
        return new FileDownload(storage.read(f.storageKey), f.contentType, f.originalName);
    }

    public PageResponse<FileResponse> list(UUID userId, PageRequest page) {
        List<FileResponse> content = fileRepository.listActiveByUser(userId, page).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(content, page, fileRepository.countActiveByUser(userId));
    }

    @Transactional
    public void delete(UUID userId, UUID fileId) {
        StoredFile f = requireOwned(userId, fileId);
        Instant now = Instant.now();
        for (FileLink link : fileLinkRepository.listActiveByFile(fileId)) {
            link.deletedAt = now;
        }
        f.deletedAt = now;
        storage.delete(f.storageKey);
    }

    StoredFile requireOwned(UUID userId, UUID fileId) {
        return fileRepository.findActiveByIdAndUser(fileId, userId)
                .orElseThrow(() -> new NotFoundException("Arquivo nao encontrado: " + fileId));
    }

    FileResponse toResponse(StoredFile f) {
        return new FileResponse(f.id, f.originalName, f.contentType, f.sizeBytes, f.sha256, f.createdAt);
    }

    private Set<String> allowedContentTypes() {
        return Arrays.stream(allowedContentTypesCsv.split(","))
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return "arquivo";
        }
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1);
        return base.length() > 255 ? base.substring(0, 255) : base;
    }

    private static String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }
}
