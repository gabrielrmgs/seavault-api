package br.dev.irontech.seavault.alerts.job;

import br.dev.irontech.seavault.alerts.service.AlertService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AlertScheduler {

    private static final Logger LOG = Logger.getLogger(AlertScheduler.class);

    private final AlertService alertService;

    public AlertScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "{seavault.alerts.cron}")
    void daily() {
        LOG.info("Iniciando varredura diaria de alertas");
        try {
            alertService.runDailyScan();
            LOG.info("Varredura diaria de alertas concluida");
        } catch (RuntimeException e) {
            LOG.error("Falha na varredura diaria de alertas", e);
        }
    }
}
