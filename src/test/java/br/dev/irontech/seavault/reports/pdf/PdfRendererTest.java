package br.dev.irontech.seavault.reports.pdf;

import br.dev.irontech.seavault.reports.pdf.ReportDocument.Field;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Section;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Table;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class PdfRendererTest {

    @Inject
    PdfRenderer renderer;

    @Test
    void rendersValidPdfBytes() {
        ReportDocument doc = new ReportDocument(
                "DOCUMENTS",
                "Relatório de Documentos",
                Instant.now(),
                List.of(new Section(
                        "Documentos",
                        List.of(new Field("Total", "1")),
                        new Table(
                                List.of("Tipo", "Número"),
                                List.of(List.of("CIR", "123"))))));

        byte[] pdf = renderer.render(doc);

        assertTrue(pdf.length > 100, "PDF deve ter conteúdo");
        // Cabecalho magico %PDF-
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
        assertEquals('-', (char) pdf[4]);
    }

    @Test
    void rendersSectionWithoutTable() {
        ReportDocument doc = new ReportDocument(
                "SEATIME",
                "Tempo de Embarque",
                Instant.now(),
                List.of(new Section("Totais", List.of(new Field("Total de dias", "120")), null)));

        byte[] pdf = renderer.render(doc);

        assertTrue(pdf.length > 100);
        assertEquals('%', (char) pdf[0]);
    }
}
