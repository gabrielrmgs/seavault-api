package br.dev.irontech.seavault.reports.dto;

import java.util.Set;

public record ReportOptions(boolean includeSensitive, Set<String> sections) {

    public boolean wants(String key) {
        return sections == null || sections.isEmpty() || sections.contains(key);
    }
}
