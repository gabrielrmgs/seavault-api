package br.dev.irontech.seavault.common.error;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) { super(message); }
    @Override public int status() { return 401; }
    @Override public String code() { return "UNAUTHORIZED"; }
}
