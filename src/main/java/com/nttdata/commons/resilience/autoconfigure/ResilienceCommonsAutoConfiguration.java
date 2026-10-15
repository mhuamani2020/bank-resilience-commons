package com.nttdata.commons.resilience.autoconfigure;

import com.nttdata.commons.resilience.error.StandardApiExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Autoconfiguración del módulo de resiliencia.
 * <p>
 * Registra el {@link StandardApiExceptionHandler} en cualquier microservicio reactivo que incluya
 * la dependencia {@code bank-resilience-commons}. Al ser autoconfiguración no requiere
 * {@code @ComponentScan} sobre el paquete {@code com.nttdata.commons}.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ResilienceCommonsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StandardApiExceptionHandler standardApiExceptionHandler() {
        return new StandardApiExceptionHandler();
    }
}
