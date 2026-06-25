package br.dev.irontech.seavault.common.error;

public abstract class AppException extends RuntimeException {
    protected AppException(String message) {
        super(message);
    }

    public abstract int status();

    public abstract String code();
}
