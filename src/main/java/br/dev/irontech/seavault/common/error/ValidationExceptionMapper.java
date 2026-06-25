package br.dev.irontech.seavault.common.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.List;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        List<FieldError> fields = ex.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();
        ApiError body = new ApiError(
                Instant.now().toString(),
                400,
                "VALIDATION",
                "Dados inválidos",
                fields
        );
        return Response.status(400).entity(body).build();
    }

    private FieldError toFieldError(ConstraintViolation<?> v) {
        String path = v.getPropertyPath().toString();
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return new FieldError(field, v.getMessage());
    }
}
