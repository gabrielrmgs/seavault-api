package br.dev.irontech.seavault.common.error;

import java.util.List;

public record ApiError(
        String timestamp,
        int status,
        String code,
        String message,
        List<FieldError> fieldErrors
) {}
