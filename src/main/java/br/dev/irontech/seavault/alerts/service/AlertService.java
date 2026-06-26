package br.dev.irontech.seavault.alerts.service;

import br.dev.irontech.seavault.alerts.domain.Alert;
import br.dev.irontech.seavault.alerts.domain.AlertSource;
import br.dev.irontech.seavault.alerts.domain.AlertStatus;
import br.dev.irontech.seavault.alerts.domain.AlertType;
import br.dev.irontech.seavault.alerts.dto.AlertResponse;
import br.dev.irontech.seavault.alerts.repo.AlertRepository;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.common.error.BadRequestException;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.common.scan.DueItem;
import br.dev.irontech.seavault.courses.service.CourseService;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.notifications.EmailService;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class AlertService {

    private static final int[] WINDOWS = {7, 15, 30, 60, 90};

    private final AlertRepository alertRepository;
    private final DocumentService documentService;
    private final CertificateService certificateService;
    private final CourseService courseService;
    private final VoyageService voyageService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @ConfigProperty(name = "seavault.alerts.max-window-days")
    int maxWindowDays;

    @ConfigProperty(name = "seavault.alerts.voyage-reminder-days")
    int voyageReminderDays;

    public AlertService(AlertRepository alertRepository,
                        DocumentService documentService,
                        CertificateService certificateService,
                        CourseService courseService,
                        VoyageService voyageService,
                        EmailService emailService,
                        UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.documentService = documentService;
        this.certificateService = certificateService;
        this.courseService = courseService;
        this.voyageService = voyageService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public PageResponse<AlertResponse> list(UUID userId, AlertStatus status, PageRequest page) {
        List<Alert> rows = (status == null)
                ? alertRepository.listByUser(userId, page)
                : alertRepository.listByUserAndStatus(userId, status, page);
        long total = (status == null)
                ? alertRepository.countByUser(userId)
                : alertRepository.countByUserAndStatus(userId, status);
        return PageResponse.of(rows.stream().map(AlertService::toResponse).toList(), page, total);
    }

    @Transactional
    public AlertResponse changeStatus(UUID userId, UUID id, AlertStatus status) {
        if (status == AlertStatus.PENDENTE) {
            throw new BadRequestException("Status inválido para alteração manual: PENDENTE");
        }
        Alert a = alertRepository.findByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Alerta nao encontrado: " + id));
        a.status = status;
        if (status == AlertStatus.RESOLVIDO) {
            a.resolvedAt = Instant.now();
        }
        return toResponse(a);
    }

    public List<AlertResponse> upcoming(UUID userId, int limit) {
        return alertRepository.listPendingByUser(userId).stream()
                .limit(limit)
                .map(AlertService::toResponse)
                .toList();
    }

    private static AlertResponse toResponse(Alert a) {
        return new AlertResponse(a.id, a.source, a.sourceId, a.type, a.title,
                a.dueDate, a.leadDays, a.status, a.createdAt, a.updatedAt);
    }

    @Transactional
    public void runDailyScan() {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(maxWindowDays);
        LocalDate cutoff = today.minusDays(voyageReminderDays);

        List<Candidate> candidates = new ArrayList<>();
        for (DueItem i : documentService.dueForAlerts(maxDate)) {
            candidates.add(new Candidate(AlertSource.DOCUMENT, AlertType.DOCUMENT_EXPIRY, i));
        }
        for (DueItem i : certificateService.dueForAlerts(maxDate)) {
            candidates.add(new Candidate(AlertSource.CERTIFICATE, AlertType.CERTIFICATE_EXPIRY, i));
        }
        for (DueItem i : courseService.dueForAlerts(maxDate)) {
            candidates.add(new Candidate(AlertSource.COURSE, AlertType.COURSE_START, i));
        }
        for (DueItem i : voyageService.dueForAlerts(cutoff)) {
            candidates.add(new Candidate(AlertSource.VOYAGE, AlertType.VOYAGE_REMINDER, i));
        }

        Set<String> qualifyingKeys = new HashSet<>();
        Set<UUID> usersWithNewAlerts = new LinkedHashSet<>();
        for (Candidate c : candidates) {
            qualifyingKeys.add(key(c.source, c.item.sourceId()));
            if (upsert(c, today)) {
                usersWithNewAlerts.add(c.item.userId());
            }
        }

        for (Alert a : alertRepository.listOpenAllUsers()) {
            if (!qualifyingKeys.contains(key(a.source, a.sourceId))) {
                a.status = AlertStatus.RESOLVIDO;
                a.resolvedAt = Instant.now();
            }
        }

        for (UUID userId : usersWithNewAlerts) {
            userRepository.findByIdOptional(userId)
                    .ifPresent(u -> emailService.sendAlertDigest(u.email, digestSummary(userId)));
        }
    }

    private boolean upsert(Candidate c, LocalDate today) {
        Optional<Alert> existing = alertRepository.findBySource(c.item.userId(), c.source, c.item.sourceId());
        int lead = leadDays(c.item.dueDate(), today);
        if (existing.isEmpty()) {
            Alert a = new Alert();
            a.userId = c.item.userId();
            a.source = c.source;
            a.sourceId = c.item.sourceId();
            a.type = c.type;
            a.title = c.item.title();
            a.dueDate = c.item.dueDate();
            a.leadDays = lead;
            a.status = AlertStatus.PENDENTE;
            alertRepository.persist(a);
            return true;
        }
        Alert a = existing.get();
        if (a.status == AlertStatus.PENDENTE || a.status == AlertStatus.LIDO) {
            a.title = c.item.title();
            a.dueDate = c.item.dueDate();
            a.leadDays = lead;
        }
        return false;
    }

    private static int leadDays(LocalDate dueDate, LocalDate today) {
        if (dueDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(today, dueDate);
        if (days < 0) {
            return 0;
        }
        for (int w : WINDOWS) {
            if (days <= w) {
                return w;
            }
        }
        return WINDOWS[WINDOWS.length - 1];
    }

    private String digestSummary(UUID userId) {
        List<Alert> pending = alertRepository.listPendingByUser(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("Você tem ").append(pending.size()).append(" alerta(s) pendente(s) no SeaVault:\n");
        for (Alert a : pending) {
            sb.append("- ").append(a.title);
            if (a.dueDate != null) {
                sb.append(" (").append(a.dueDate).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String key(AlertSource source, UUID sourceId) {
        return source.name() + ":" + sourceId;
    }

    private record Candidate(AlertSource source, AlertType type, DueItem item) {}
}
