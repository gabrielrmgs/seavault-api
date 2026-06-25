package br.dev.irontech.seavault.common.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ValidationExceptionMapperTest {

    record Sample(@NotBlank String name) {}

    @Test
    void mapsConstraintViolationsTo400WithFieldErrors() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Sample>> violations = validator.validate(new Sample(""));

        Response response = new ValidationExceptionMapper()
                .toResponse(new ConstraintViolationException(violations));

        assertEquals(400, response.getStatus());
        ApiError body = assertInstanceOf(ApiError.class, response.getEntity());
        assertEquals("VALIDATION", body.code());
        assertFalse(body.fieldErrors().isEmpty());
    }
}
