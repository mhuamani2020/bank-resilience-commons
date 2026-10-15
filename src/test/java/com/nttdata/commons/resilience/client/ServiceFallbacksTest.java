package com.nttdata.commons.resilience.client;

import com.nttdata.commons.resilience.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class ServiceFallbacksTest {

    @Test
    void unavailableEmitsServiceUnavailableException() {
        StepVerifier.create(ServiceFallbacks.unavailable(ServiceNames.CREDIT, new RuntimeException("timeout")))
                .expectErrorSatisfies(error -> {
                    assertThatTypeIsServiceUnavailable(error);
                    assertThatMessageContainsService(error);
                })
                .verify();
    }

    private void assertThatTypeIsServiceUnavailable(Throwable error) {
        if (!(error instanceof ServiceUnavailableException)) {
            throw new AssertionError("Se esperaba ServiceUnavailableException pero fue " + error.getClass());
        }
    }

    private void assertThatMessageContainsService(Throwable error) {
        if (!error.getMessage().contains(ServiceNames.CREDIT)) {
            throw new AssertionError("El mensaje no menciona el servicio destino: " + error.getMessage());
        }
    }
}
