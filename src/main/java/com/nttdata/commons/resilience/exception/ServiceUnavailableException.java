package com.nttdata.commons.resilience.exception;

/**
 * Excepción que representa la <b>indisponibilidad de una dependencia</b> (otro microservicio),
 * no un error de negocio. Se mapea a HTTP 503 en {@code StandardApiExceptionHandler}.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
