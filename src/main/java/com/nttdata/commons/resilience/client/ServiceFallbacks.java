package com.nttdata.commons.resilience.client;

import com.nttdata.commons.resilience.exception.ServiceUnavailableException;
import reactor.core.publisher.Mono;

/**
 * Fallbacks estandarizados para llamadas entre microservicios.
 * <p>
 * Regla del estándar: un fallo de <b>disponibilidad</b> (timeout, error de conexión,
 * 5xx, circuit breaker abierto) nunca debe degradarse a un error de negocio. Se traduce
 * siempre a {@link ServiceUnavailableException} que el manejador global expone como HTTP 503.
 */
public final class ServiceFallbacks {

    private ServiceFallbacks() {
        // Clase de utilidades
    }

    /**
     * Genera un {@code Mono} en error indicando que el servicio destino no está disponible.
     *
     * @param targetService nombre lógico del servicio destino (ver {@link ServiceNames})
     * @param cause         causa original (timeout, error de red, excepción del circuit breaker, ...)
     * @return un {@code Mono} que emite error con {@link ServiceUnavailableException}
     */
    public static <T> Mono<T> unavailable(String targetService, Throwable cause) {
        return Mono.error(new ServiceUnavailableException(
                "El servicio " + targetService + " no está disponible temporalmente.", cause));
    }
}
