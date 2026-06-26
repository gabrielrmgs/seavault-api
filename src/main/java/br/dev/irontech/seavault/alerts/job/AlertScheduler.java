package br.dev.irontech.seavault.alerts.job;

import br.dev.irontech.seavault.alerts.service.AlertService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertScheduler {

    private final AlertService alertService;

    public AlertScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "{seavault.alerts.cron}")
    void daily() {
        alertService.runDailyScan();
    }
}
