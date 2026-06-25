package br.dev.irontech.seavault.common.error;

public class NotFoundException extends AppException {
    public NotFoundException(String message) { super(message); }
    @Override public int status() { return 404; }
    @Override public String code() { return "NOT_FOUND"; }
}
