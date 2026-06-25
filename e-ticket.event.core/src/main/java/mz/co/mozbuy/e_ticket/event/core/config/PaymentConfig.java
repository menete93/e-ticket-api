package mz.co.mozbuy.e_ticket.event.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class PaymentConfig {
    // Configurações adicionais podem ser adicionadas aqui
}
