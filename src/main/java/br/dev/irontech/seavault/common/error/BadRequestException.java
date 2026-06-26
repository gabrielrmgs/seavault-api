package br.dev.irontech.seavault.common.error;

public class BadRequestException extends AppException {
    public BadRequestException(String message) { super(message); }
    @Override public int status() { return 400; }
    @Override public String code() { return "VALIDATION"; }
}
