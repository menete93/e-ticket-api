package mz.co.mozbuy.e_ticket.event.core.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiryScheduler {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserEventReservationControlRepository controlRepository;

    @Scheduled(fixedDelay = 60000) // Executa a cada minuto
    @Transactional
    public void expireExpiredTransactions() {
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = 0;

        // Buscar transações PENDING que expiraram
        List<PaymentTransactionEntity> expiredTransactions = paymentTransactionRepository
                .findByStatusAndExpiresAtBefore("PENDING", now);

        for (PaymentTransactionEntity transaction : expiredTransactions) {
            log.info("⏰ Expirando reserva: {} - Código: {}",
                    transaction.getId(), transaction.getReservationCode());

            // 1. Marcar transação como EXPIRED
            transaction.markExpired();
            paymentTransactionRepository.save(transaction);

            // 2. Liberar controle de reserva do usuário (se existir)
            if (transaction.getUserId() != null) {
                controlRepository.findByUserIdAndEventId(transaction.getUserId(), transaction.getEventId())
                        .ifPresent(control -> {
                            control.releaseActiveReservation();
                            controlRepository.save(control);
                            log.info("🔓 Reserva liberada para usuário: {} no evento: {}",
                                    transaction.getUserId(), transaction.getEventId());
                        });
            }

            expiredCount++;
        }

        if (expiredCount > 0) {
            log.info("⏰ Expired {} pending reservation(s)", expiredCount);
        }
    }

    /**
     * Opcional: Limpar tentativas antigas (opcional, pode ser executado diariamente)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Executa às 2h da manhã
    @Transactional
    public void cleanupOldFailedAttempts() {
        // Se quiser limpar tentativas antigas, implementar com PaymentAttemptRepository
        log.info("🧹 Cleanup de dados antigos (implementar se necessário)");
    }
}