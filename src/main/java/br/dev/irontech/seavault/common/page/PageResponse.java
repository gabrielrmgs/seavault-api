package br.dev.irontech.seavault.common.page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, PageRequest req, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / req.size());
        return new PageResponse<>(content, req.page(), req.size(), totalElements, totalPages);
    }
}
