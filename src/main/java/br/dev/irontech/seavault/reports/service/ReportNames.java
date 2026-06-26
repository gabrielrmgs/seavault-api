package br.dev.irontech.seavault.reports.service;

import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.companies.service.CompanyService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import br.dev.irontech.seavault.vessels.service.VesselService;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.UUID;

@ApplicationScoped
public class ReportNames {

    private final VesselService vesselService;
    private final CompanyService companyService;
    private final ReferenceRepository referenceRepository;

    public ReportNames(VesselService vesselService,
                       CompanyService companyService,
                       ReferenceRepository referenceRepository) {
        this.vesselService = vesselService;
        this.companyService = companyService;
        this.referenceRepository = referenceRepository;
    }

    public String vesselName(UUID userId, UUID id) {
        if (id == null) {
            return "—";
        }
        try {
            return vesselService.get(userId, id).name();
        } catch (NotFoundException e) {
            return "—";
        }
    }

    public String companyName(UUID userId, UUID id) {
        if (id == null) {
            return "—";
        }
        try {
            return companyService.get(userId, id).name();
        } catch (NotFoundException e) {
            return "—";
        }
    }

    public String categoryName(UUID id) {
        if (id == null) {
            return "—";
        }
        return referenceRepository.findCategoryById(id).map(c -> c.name).orElse("—");
    }

    public String typeLabel(UUID typeId) {
        if (typeId == null) {
            return "—";
        }
        return referenceRepository.findTypeById(typeId).map(t -> t.label).orElse("—");
    }

    public static String nz(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    public static String dateStr(LocalDate d) {
        return d == null ? "—" : d.toString();
    }

    public static String str(long n) {
        return String.valueOf(n);
    }
}
