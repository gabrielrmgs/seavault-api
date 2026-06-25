package br.dev.irontech.seavault.files.dto;

import java.time.Instant;
import java.util.UUID;

public record FileResponse(
        UUID id,
        String originalName,
        String contentType,
        long sizeBytes,
        String sha256,
        Instant createdAt
) {}
