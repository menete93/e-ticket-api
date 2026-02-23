package mz.co.mozbuy.e_ticket.event.auth.feignClient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Política de retry: máximo de 3 tentativas
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Política de backoff: esperar 1 segundo, 2, 4, etc. entre tentativas
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L); // 1 segundo
        backOffPolicy.setMultiplier(2.0); // dobra a cada tentativa
        backOffPolicy.setMaxInterval(10000L); // máximo de 10 segundos

        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}