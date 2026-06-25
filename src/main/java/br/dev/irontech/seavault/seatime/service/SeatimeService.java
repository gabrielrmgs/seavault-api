package br.dev.irontech.seavault.seatime.service;

import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse.EntityDays;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse.LastVoyage;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse.NavTypeDays;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse.RoleDays;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse.YearDays;
import br.dev.irontech.seavault.voyages.dto.VoyageResponse;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SeatimeService {

    private final VoyageService voyageService;

    public SeatimeService(VoyageService voyageService) {
        this.voyageService = voyageService;
    }

    public SeatimeSummaryResponse summary(UUID userId) {
        List<VoyageResponse> all = voyageService.listAllForUser(userId);
        List<VoyageResponse> finished = all.stream().filter(v -> "FINISHED".equals(v.status())).toList();
        List<VoyageResponse> active = all.stream().filter(v -> "ACTIVE".equals(v.status())).toList();
        LocalDate today = LocalDate.now();

        long totalDays = finished.stream().mapToLong(v -> days(v)).sum();
        long activeDays = active.stream().mapToLong(v -> days(v)).sum();

        long last12 = finished.stream()
                .filter(v -> v.disembarkDate() != null && !v.disembarkDate().isBefore(today.minusMonths(12)))
                .mapToLong(v -> days(v)).sum();
        long last5y = finished.stream()
                .filter(v -> v.disembarkDate() != null && !v.disembarkDate().isBefore(today.minusYears(5)))
                .mapToLong(v -> days(v)).sum();

        double avg = finished.isEmpty() ? 0.0 : (double) totalDays / finished.size();

        Integer longest = finished.stream().map(v -> v.effectiveDays())
                .filter(d -> d != null).max(Comparator.naturalOrder()).orElse(null);

        Long daysSinceLastDisembark = finished.stream()
                .map(VoyageResponse::disembarkDate).filter(d -> d != null)
                .max(Comparator.naturalOrder())
                .map(last -> ChronoUnit.DAYS.between(last, today)).orElse(null);

        LastVoyage lastVoyage = all.stream()
                .max(Comparator.comparing(VoyageResponse::embarkDate))
                .map(v -> new LastVoyage(v.id(), v.vesselId(), v.embarkDate(), v.disembarkDate(), v.status()))
                .orElse(null);

        List<RoleDays> byRole = sumBy(finished, VoyageResponse::role).entrySet().stream()
                .map(e -> new RoleDays(e.getKey(), e.getValue())).toList();
        List<EntityDays> byCompany = sumBy(finished, VoyageResponse::companyId).entrySet().stream()
                .map(e -> new EntityDays(e.getKey(), e.getValue())).toList();
        List<EntityDays> byVessel = sumBy(finished, VoyageResponse::vesselId).entrySet().stream()
                .map(e -> new EntityDays(e.getKey(), e.getValue())).toList();
        List<NavTypeDays> byNavigationType = sumBy(finished, VoyageResponse::navigationTypeId).entrySet().stream()
                .map(e -> new NavTypeDays(e.getKey(), e.getValue())).toList();
        List<YearDays> byYear = sumBy(finished, v -> v.embarkDate().getYear()).entrySet().stream()
                .map(e -> new YearDays(e.getKey(), e.getValue())).toList();

        List<String> warnings = new ArrayList<>();
        if (active.size() > 1) {
            warnings.add("Multiplos embarques ativos: " + active.size());
        }
        if (hasOverlap(all, today)) {
            warnings.add("Embarques sobrepostos detectados");
        }

        return new SeatimeSummaryResponse(totalDays, activeDays, last12, last5y, avg, longest,
                daysSinceLastDisembark, lastVoyage, byRole, byCompany, byVessel, byNavigationType, byYear, warnings);
    }

    private static long days(VoyageResponse v) {
        return v.effectiveDays() == null ? 0L : v.effectiveDays();
    }

    private static <K> Map<K, Long> sumBy(List<VoyageResponse> voyages, java.util.function.Function<VoyageResponse, K> key) {
        Map<K, Long> out = new LinkedHashMap<>();
        for (VoyageResponse v : voyages) {
            out.merge(key.apply(v), days(v), Long::sum);
        }
        return out;
    }

    private static boolean hasOverlap(List<VoyageResponse> voyages, LocalDate today) {
        List<VoyageResponse> sorted = voyages.stream()
                .sorted(Comparator.comparing(VoyageResponse::embarkDate)).toList();
        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i + 1; j < sorted.size(); j++) {
                LocalDate aStart = sorted.get(i).embarkDate();
                LocalDate aEnd = sorted.get(i).disembarkDate() != null ? sorted.get(i).disembarkDate() : today;
                LocalDate bStart = sorted.get(j).embarkDate();
                LocalDate bEnd = sorted.get(j).disembarkDate() != null ? sorted.get(j).disembarkDate() : today;
                if (!aEnd.isBefore(bStart) && !bEnd.isBefore(aStart)) {
                    return true;
                }
            }
        }
        return false;
    }
}
