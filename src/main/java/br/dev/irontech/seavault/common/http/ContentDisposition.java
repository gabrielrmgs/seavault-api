package br.dev.irontech.seavault.common.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class ContentDisposition {

    private ContentDisposition() {
    }

    public static String attachment(String rawName) {
        String name = (rawName == null || rawName.isBlank()) ? "arquivo" : rawName;
        String clean = name.replaceAll("[\\r\\n\"]", "")
                .replaceAll("[\\x00-\\x1F\\x7F]", "");
        String asciiToken = clean.replaceAll("[^A-Za-z0-9._\\- ]", "_");
        if (asciiToken.isBlank()) {
            asciiToken = "arquivo";
        }
        String encoded = URLEncoder.encode(clean, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + asciiToken + "\"; filename*=UTF-8''" + encoded;
    }
}
