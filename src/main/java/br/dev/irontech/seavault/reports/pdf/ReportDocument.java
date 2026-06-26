package br.dev.irontech.seavault.reports.pdf;

import java.time.Instant;
import java.util.List;

/** Modelo neutro de relatorio: serve a JSON (serializado direto) e a PDF (via PdfRenderer). */
public record ReportDocument(String type, String title, Instant generatedAt, List<Section> sections) {

    public record Section(String heading, List<Field> fields, Table table) {}

    public record Field(String label, String value) {}

    public record Table(List<String> columns, List<List<String>> rows) {}
}
