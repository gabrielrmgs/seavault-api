package br.dev.irontech.seavault.dashboard.service;

import br.dev.irontech.seavault.alerts.dto.AlertResponse;
import br.dev.irontech.seavault.alerts.service.AlertService;
import br.dev.irontech.seavault.certificates.dto.CertificateResponse;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.common.expiry.ExpiryStatus;
import br.dev.irontech.seavault.courses.domain.CourseStatus;
import br.dev.irontech.seavault.courses.dto.CourseResponse;
import br.dev.irontech.seavault.courses.service.CourseService;
import br.dev.irontech.seavault.dashboard.dto.DashboardResponse;
import br.dev.irontech.seavault.dashboard.dto.DashboardResponse.Counts;
import br.dev.irontech.seavault.dashboard.dto.DashboardResponse.CourseCounts;
import br.dev.irontech.seavault.dashboard.dto.DashboardResponse.SeatimeBlock;
import br.dev.irontech.seavault.documents.dto.DocumentResponse;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.profile.service.ProfileService;
import br.dev.irontech.seavault.seatime.dto.SeatimeSummaryResponse;
import br.dev.irontech.seavault.seatime.service.SeatimeService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DashboardService {

    private final DocumentService documentService;
    private final CertificateService certificateService;
    private final CourseService courseService;
    private final SeatimeService seatimeService;
    private final ProfileService profileService;
    private final AlertService alertService;

    public DashboardService(DocumentService documentService,
                            CertificateService certificateService,
                            CourseService courseService,
                            SeatimeService seatimeService,
                            ProfileService profileService,
                            AlertService alertService) {
        this.documentService = documentService;
        this.certificateService = certificateService;
        this.courseService = courseService;
        this.seatimeService = seatimeService;
        this.profileService = profileService;
        this.alertService = alertService;
    }

    public DashboardResponse summary(UUID userId) {
        Counts documents = expiryCounts(documentService.listAllForUser(userId).stream()
                .map(DocumentResponse::status).toList());
        Counts certificates = expiryCounts(certificateService.listAllForUser(userId).stream()
                .map(CertificateResponse::status).toList());

        List<CourseResponse> courses = courseService.listAllForUser(userId);
        long completed = courses.stream().filter(c -> c.status() == CourseStatus.CONCLUIDO).count();
        long pending = courses.stream()
                .filter(c -> c.status() == CourseStatus.PLANEJADO || c.status() == CourseStatus.EM_ANDAMENTO)
                .count();

        SeatimeSummaryResponse st = seatimeService.summary(userId);
        SeatimeBlock seatime = new SeatimeBlock(st.totalDays(), st.activeDays(), st.lastVoyage());

        int completion = profileService.getOrCreate(userId).completionPercent();
        List<AlertResponse> upcoming = alertService.upcoming(userId, 5);

        return new DashboardResponse(documents, certificates, seatime,
                new CourseCounts(completed, pending), completion, upcoming);
    }

    private static Counts expiryCounts(List<ExpiryStatus> statuses) {
        long total = statuses.size();
        long valid = statuses.stream().filter(s -> s == ExpiryStatus.VALIDO).count();
        long expiring = statuses.stream().filter(s -> s == ExpiryStatus.VENCENDO).count();
        long expired = statuses.stream().filter(s -> s == ExpiryStatus.VENCIDO).count();
        return new Counts(total, valid, expiring, expired);
    }
}
