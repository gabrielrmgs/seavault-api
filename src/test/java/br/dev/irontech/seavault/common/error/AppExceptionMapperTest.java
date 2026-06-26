package br.dev.irontech.seavault.common.error;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppExceptionMapperTest {

    @Test
    void mapsConflictToStatusAndCode() {
        AppExceptionMapper mapper = new AppExceptionMapper();
        Response response = mapper.toResponse(new ConflictException("e-mail já cadastrado"));

        assertEquals(409, response.getStatus());
        ApiError body = assertInstanceOf(ApiError.class, response.getEntity());
        assertEquals("CONFLICT", body.code());
        assertEquals("e-mail já cadastrado", body.message());
    }

    @Test
    void mapsNotFoundToStatus404() {
        AppExceptionMapper mapper = new AppExceptionMapper();
        Response response = mapper.toResponse(new NotFoundException("não encontrado"));

        assertEquals(404, response.getStatus());
    }

    @Test
    void mapsBusinessExceptionWithFieldErrors() {
        AppExceptionMapper mapper = new AppExceptionMapper();
        Response response = mapper.toResponse(new BusinessException(
                "Anexo 1-S incompleto",
                List.of(new FieldError("cpf", "CPF obrigatorio"))));

        assertEquals(422, response.getStatus());
        ApiError body = assertInstanceOf(ApiError.class, response.getEntity());
        assertEquals("BUSINESS_RULE", body.code());
        assertEquals(1, body.fieldErrors().size());
        assertEquals("cpf", body.fieldErrors().get(0).field());
    }

    @Test
    void businessExceptionWithoutFieldsHasEmptyList() {
        AppExceptionMapper mapper = new AppExceptionMapper();
        Response response = mapper.toResponse(new BusinessException("regra de negocio"));

        ApiError body = assertInstanceOf(ApiError.class, response.getEntity());
        assertTrue(body.fieldErrors().isEmpty());
    }
}
