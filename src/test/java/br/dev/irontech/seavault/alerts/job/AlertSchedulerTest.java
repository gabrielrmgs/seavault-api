package br.dev.irontech.seavault.alerts.job;

import br.dev.irontech.seavault.alerts.service.AlertService;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class AlertSchedulerTest {

    @Inject
    AlertScheduler scheduler;

    @Test
    void schedulerSwallowsExceptionsFromScan() {
        AlertService failing = Mockito.mock(AlertService.class);
        Mockito.doThrow(new RuntimeException("boom")).when(failing).runDailyScan();
        QuarkusMock.installMockForType(failing, AlertService.class);

        scheduler.daily();

        Mockito.verify(failing).runDailyScan();
    }
}
