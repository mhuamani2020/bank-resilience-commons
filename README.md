# bank-resilience-commons

Módulo compartido con el **estándar de resiliencia** del sistema bancario de microservicios.
Lo consumen los servicios que llaman a otros servicios (`bank-account-service` y
`credit-service`) para no duplicar configuración ni código de tolerancia a fallos.

Es una librería, no un microservicio desplegable: no expone puertos ni endpoints propios.
Se instala en el repositorio Maven local (`~/.m2`) y los servicios la declaran como
dependencia normal.

## Contenido

| Paquete | Clase | Responsabilidad |
|---|---|---|
| `client` | `ServiceNames` | Nombres canónicos de los servicios y de las instancias de circuit breaker (`CUSTOMER_CB`, `CREDIT_CB`), para no repetir literales en cada servicio. |
| `client` | `ServiceFallbacks` | Métodos de *fallback* reactivos estándar: convierten un fallo de disponibilidad en `ServiceUnavailableException`. |
| `exception` | `ServiceUnavailableException` | Excepción de disponibilidad (HTTP **503**), distinta de los errores de negocio (HTTP **400**). |
| `error` | `ApiError` | Cuerpo de error uniforme de todas las APIs. |
| `error` | `StandardApiExceptionHandler` | Manejador `@RestControllerAdvice` común: `IllegalArgumentException` → 400, `ServiceUnavailableException` → 503, error inesperado → 500. |
| `autoconfigure` | `ResilienceCommonsAutoConfiguration` | Autoconfiguración registrada en `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, de modo que basta con añadir la dependencia para que el manejador de errores quede activo. |

## Criterio de errores

| Situación | Excepción | HTTP |
|---|---|---|
| Regla de negocio incumplida (cliente inexistente, tipo de producto no permitido, límite excedido…) | `IllegalArgumentException` | 400 |
| Servicio consumido caído, lento o con el circuito abierto | `ServiceUnavailableException` | 503 |
| Cualquier otro fallo no controlado | — | 500 |

Mantener separadas ambas categorías es lo que permite que un *fallback* por indisponibilidad
nunca se confunda con un error de validación del negocio.

## Uso

La URL de cada servicio consumido **no** se escribe en el código: se inyecta desde la
configuración externalizada.

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerClientAdapter implements CustomerClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.urls.customer-service}")
    private String customerServiceUrl;

    @Override
    @CircuitBreaker(name = ServiceNames.CUSTOMER_CB, fallbackMethod = "customerUnavailable")
    @TimeLimiter(name = ServiceNames.CUSTOMER_CB)
    public Mono<CustomerDTO> getCustomerById(String customerId) {
        // ...
    }

    private Mono<CustomerDTO> customerUnavailable(Throwable ex) {
        return ServiceFallbacks.unavailable(ex, "customer-service");
    }
}
```

La configuración de las instancias de Resilience4j (ventana deslizante, umbral de fallo,
**timeout de 2 segundos**) vive en los ficheros YAML de cada servicio y en
`bank-config-repo` (Config Server), nunca en el código.

## Requisitos

- Java 17
- Spring Boot 3 (WebFlux)
- Resilience4j (`resilience4j-spring-boot3`)

## Construcción e instalación

```bash
./mvnw -B -ntp clean install
```

`install` es necesario para que el artefacto quede en el repositorio local y los
microservicios puedan resolver la dependencia:

```xml
<dependency>
    <groupId>com.nttdata</groupId>
    <artifactId>bank-resilience-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Pruebas

```bash
./mvnw -B -ntp test
```
