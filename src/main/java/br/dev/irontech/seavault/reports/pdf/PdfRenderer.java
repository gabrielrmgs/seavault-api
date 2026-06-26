package br.dev.irontech.seavault.reports.pdf;

import br.dev.irontech.seavault.reports.pdf.ReportDocument.Field;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Section;
import br.dev.irontech.seavault.reports.pdf.ReportDocument.Table;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayOutputStream;
import java.util.List;

@ApplicationScoped
public class PdfRenderer {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font META_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FIELD_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font CELL_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

    public byte[] render(ReportDocument doc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.A4, 36, 36, 54, 36);
        try {
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            Paragraph title = new Paragraph(doc.title(), TITLE_FONT);
            title.setSpacingAfter(4f);
            pdf.add(title);

            pdf.add(new Paragraph("Gerado em: " + doc.generatedAt(), META_FONT));

            for (Section section : doc.sections()) {
                Paragraph heading = new Paragraph(section.heading(), HEADING_FONT);
                heading.setSpacingBefore(12f);
                heading.setSpacingAfter(4f);
                pdf.add(heading);

                if (section.fields() != null) {
                    for (Field f : section.fields()) {
                        pdf.add(new Paragraph(f.label() + ": " + nz(f.value()), FIELD_FONT));
                    }
                }

                if (section.table() != null) {
                    pdf.add(buildTable(section.table()));
                }
            }

            pdf.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Falha ao gerar PDF do relatório", e);
        }
    }

    private static PdfPTable buildTable(Table table) {
        List<String> columns = table.columns();
        PdfPTable pt = new PdfPTable(Math.max(1, columns.size()));
        pt.setWidthPercentage(100f);
        pt.setSpacingBefore(4f);
        for (String col : columns) {
            PdfPCell cell = new PdfPCell(new Phrase(nz(col), CELL_HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            pt.addCell(cell);
        }
        for (List<String> row : table.rows()) {
            for (String value : row) {
                pt.addCell(new PdfPCell(new Phrase(nz(value), CELL_FONT)));
            }
        }
        return pt;
    }

    private static String nz(String s) {
        return s == null ? "—" : s;
    }
}
