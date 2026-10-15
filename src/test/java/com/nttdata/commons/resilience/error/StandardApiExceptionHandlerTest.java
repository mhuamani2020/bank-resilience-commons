package com.nttdata.commons.resilience.error;

import com.nttdata.commons.resilience.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class StandardApiExceptionHandlerTest {

    private final StandardApiExceptionHandler handler = new StandardApiExceptionHandler();
    private final MockServerWebExchange exchange =
            MockServerWebExchange.from(MockServerHttpRequest.get("/api/accounts/1").build());

    @Test
    void mapsServiceUnavailableTo503() {
        ResponseEntity<ApiError> response = handler.handleServiceUnavailable(
                new ServiceUnavailableException("El servicio credit-service no está disponible temporalmente."),
                exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getPath()).isEqualTo("/api/accounts/1");
    }

    @Test
    void mapsIllegalArgumentTo400() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(
                new IllegalArgumentException("El cliente no existe"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("El cliente no existe");
    }

    @Test
    void mapsValidationErrorsTo400WithFieldDetails() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "customerId", "no debe estar vacío"));
        MethodParameter parameter = new MethodParameter(
                StandardApiExceptionHandler.class.getDeclaredMethod(
                        "handleValidation", WebExchangeBindException.class, ServerWebExchange.class),
                0);
        WebExchangeBindException exception = new WebExchangeBindException(parameter, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(exception, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("customerId").contains("no debe estar vacío");
    }

    @Test
    void mapsUnknownErrorsTo500() {
        ResponseEntity<ApiError> response = handler.handleGeneric(new RuntimeException("boom"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("boom");
    }
}
