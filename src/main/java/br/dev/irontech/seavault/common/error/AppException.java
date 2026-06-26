package br.dev.irontech.seavault.common.error;

import java.util.List;

public abstract class AppException extends RuntimeException {
    protected AppException(String message) {
        super(message);
    }

    public abstract int status();

    public abstract String code();

    public List<FieldError> fieldErrors() {
        return List.of();
    }
}
