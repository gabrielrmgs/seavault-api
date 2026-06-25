package br.dev.irontech.seavault.common.page;

public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static PageRequest of(Integer page, Integer size) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new PageRequest(p, s);
    }

    public int offset() {
        return page * size;
    }
}
