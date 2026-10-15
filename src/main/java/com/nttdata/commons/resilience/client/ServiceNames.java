package com.nttdata.commons.resilience.client;

/**
 * Nombres estandarizados de los servicios destino y de sus circuit breakers.
 * <p>
 * Convención: la instancia del circuit breaker es {@code <target>ServiceCB},
 * por lo que el nombre debe coincidir con la configuración de
 * {@code resilience4j.circuitbreaker.instances.<nombre>}.
 */
public final class ServiceNames {

    /** Nombre lógico del microservicio de clientes (usado en logs/errores). */
    public static final String CUSTOMER = "customer-service";

    /** Nombre lógico del microservicio de créditos (usado en logs/errores). */
    public static final String CREDIT = "credit-service";

    /** Instancia de circuit breaker para las llamadas salientes a customer-service. */
    public static final String CUSTOMER_CB = "customerServiceCB";

    /** Instancia de circuit breaker para las llamadas salientes a credit-service. */
    public static final String CREDIT_CB = "creditServiceCB";

    private ServiceNames() {
        // Clase de constantes
    }
}
