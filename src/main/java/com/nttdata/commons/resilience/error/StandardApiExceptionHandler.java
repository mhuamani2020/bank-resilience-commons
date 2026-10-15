package com.nttdata.commons.resilience.error;

import com.nttdata.commons.resilience.exception.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manejo de errores único y estandarizado para todos los microservicios reactivos del banco.
 * <p>
 * Se registra por autoconfiguración, por lo que cada servicio <b>no</b> debe definir su propio
 * {@code @RestControllerAdvice}. Si un servicio necesita un manejo adicional debe añadir un
 * {@code @ExceptionHandler} propio con mayor prioridad ({@code @Order}).
 * <ul>
 *   <li>{@code 400} errores de negocio / validación ({@link IllegalArgumentException}, body inválido)</li>
 *   <li>{@code 503} dependencia no disponible ({@link ServiceUnavailableException})</li>
 *   <li>{@code 500} cualquier otro error no controlado</li>
 * </ul>
 */
@RestControllerAdvice
public class StandardApiExceptionHandler {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleServiceUnavailable(ServiceUnavailableException ex,
                                                             ServerWebExchange exchange) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), exchange);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          ServerWebExchange exchange) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> handleValidation(WebExchangeBindException ex,
                                                     ServerWebExchange exchange) {
        String message = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validación");
        return build(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, ServerWebExchange exchange) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), exchange);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, ServerWebExchange exchange) {
        ApiError body = new ApiError(
                OffsetDateTime.now().format(TIMESTAMP_FORMAT),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(status).body(body);
    }
}
