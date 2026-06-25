package br.dev.irontech.seavault.common.error;

public class ConflictException extends AppException {
    public ConflictException(String message) { super(message); }
    @Override public int status() { return 409; }
    @Override public String code() { return "CONFLICT"; }
}
