package br.dev.irontech.seavault.common.error;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
}
