package br.dev.irontech.seavault.reports.domain;

import br.dev.irontech.seavault.common.error.BadRequestException;

public enum ReportFormat {
    JSON,
    PDF;

    public static ReportFormat fromString(String s) {
        if (s == null || s.isBlank()) {
            return JSON;
        }
        try {
            return ReportFormat.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato de relatório inválido: " + s);
        }
    }
}
