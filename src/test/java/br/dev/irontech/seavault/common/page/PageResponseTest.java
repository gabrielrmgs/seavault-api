package br.dev.irontech.seavault.common.page;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageResponseTest {

    @Test
    void clampsSizeAndDefaults() {
        assertEquals(20, PageRequest.of(null, null).size());
        assertEquals(0, PageRequest.of(null, null).page());
        assertEquals(100, PageRequest.of(0, 5000).size());
        assertEquals(0, PageRequest.of(-3, 10).page());
    }

    @Test
    void computesTotalPages() {
        PageRequest req = PageRequest.of(0, 10);
        PageResponse<String> resp = PageResponse.of(List.of("a", "b"), req, 25);

        assertEquals(3, resp.totalPages());
        assertEquals(25, resp.totalElements());
        assertEquals(2, resp.content().size());
    }
}
