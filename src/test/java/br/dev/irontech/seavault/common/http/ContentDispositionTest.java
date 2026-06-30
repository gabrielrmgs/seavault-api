package br.dev.irontech.seavault.common.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentDispositionTest {

    @Test
    void sanitizesDangerousFilenameCharacters() {
        String header = ContentDisposition.attachment("relatorio\"\r\nX-Evil: yes.pdf");

        assertFalse(header.contains("\r"));
        assertFalse(header.contains("\n"));
        String classicFilename = header.substring("attachment; filename=\"".length(), header.indexOf("\"; filename*"));
        assertFalse(classicFilename.contains("\""));
        assertFalse(header.contains("\"X-Evil"));
    }

    @Test
    void keepsUsefulAsciiFilename() {
        String header = ContentDisposition.attachment("doc.pdf");

        assertEquals("attachment; filename=\"doc.pdf\"; filename*=UTF-8''doc.pdf", header);
        assertTrue(header.contains("filename*=UTF-8''doc.pdf"));
    }
}
