package br.dev.irontech.seavault.common.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {

    @Override
    public Response toResponse(AppException ex) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                ex.status(),
                ex.code(),
                ex.getMessage(),
                ex.fieldErrors()
        );
        return Response.status(ex.status()).entity(body).build();
    }
}
