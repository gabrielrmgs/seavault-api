package br.dev.irontech.seavault.seatime.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SeatimeSummaryResponse(
        long totalDays,
        long activeDays,
        long last12MonthsDays,
        long last5YearsDays,
        double averageDaysPerContract,
        Integer longestContractDays,
        Long daysSinceLastDisembark,
        LastVoyage lastVoyage,
        List<RoleDays> byRole,
        List<EntityDays> byCompany,
        List<EntityDays> byVessel,
        List<NavTypeDays> byNavigationType,
        List<YearDays> byYear,
        List<String> warnings
) {
    public record RoleDays(String role, long days) {}

    public record EntityDays(UUID id, long days) {}

    public record NavTypeDays(UUID navigationTypeId, long days) {}

    public record YearDays(int year, long days) {}

    public record LastVoyage(UUID id, UUID vesselId, LocalDate embarkDate, LocalDate disembarkDate, String status) {}
}
