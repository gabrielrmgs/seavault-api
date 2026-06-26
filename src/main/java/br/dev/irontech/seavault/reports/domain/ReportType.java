package br.dev.irontech.seavault.reports.domain;

import br.dev.irontech.seavault.common.error.BadRequestException;

public enum ReportType {
    CAREER,
    SEATIME,
    CERTIFICATES,
    DOCUMENTS,
    CV,
    ANEXO_1S;

    public static ReportType fromString(String s) {
        if (s == null || s.isBlank()) {
            throw new BadRequestException("Tipo de relatório ausente");
        }
        try {
            return ReportType.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de relatório inválido: " + s);
        }
    }
}
