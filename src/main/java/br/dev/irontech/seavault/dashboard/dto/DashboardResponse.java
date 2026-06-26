package br.dev.irontech.seavault.dashboard.dto;

import br.dev.irontech.seavault.alerts.dto.AlertResponse;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse;

import java.util.List;

public record DashboardResponse(
        Counts documents,
        Counts certificates,
        SeatimeBlock seatime,
        CourseCounts courses,
        int profileCompletion,
        List<AlertResponse> upcomingAlerts
) {
    public record Counts(long total, long semValidade, long valid, long expiring, long expired) {}

    public record SeatimeBlock(long totalDays, long activeDays, SeatimeSummaryResponse.LastVoyage lastVoyage) {}

    public record CourseCounts(long completed, long pending) {}
}
