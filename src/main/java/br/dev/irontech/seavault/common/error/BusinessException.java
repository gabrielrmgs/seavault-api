package br.dev.irontech.seavault.common.error;

import java.util.List;

public class BusinessException extends AppException {

    private final List<FieldError> fieldErrors;

    public BusinessException(String message) {
        this(message, List.of());
    }

    public BusinessException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    @Override
    public int status() {
        return 422;
    }

    @Override
    public String code() {
        return "BUSINESS_RULE";
    }

    @Override
    public List<FieldError> fieldErrors() {
        return fieldErrors;
    }
}
