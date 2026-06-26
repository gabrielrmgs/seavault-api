package br.dev.irontech.seavault.reports.dto;

import br.dev.irontech.seavault.reports.domain.ReportFormat;
import br.dev.irontech.seavault.reports.domain.ReportType;

import java.time.Instant;
import java.util.UUID;

public record ReportHistoryResponse(
        UUID id,
        ReportType type,
        ReportFormat format,
        String params,
        Instant generatedAt
) {}
