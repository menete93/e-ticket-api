package mz.co.mozbuy.e_ticket.event.core.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import mz.co.mozbuy.e_ticket.event.core.model.ScheduledPriceChange;
import mz.co.mozbuy.e_ticket.event.core.repository.EventTicketRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.ScheduledPriceChangeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledPriceChangeProcessor {

    private final ScheduledPriceChangeRepository scheduledPriceChangeRepository;
    private final EventTicketRepository eventTicketRepository;

    /**
     * Processa mudanças de preço programadas a cada minuto
     */
    @Scheduled(fixedDelay = 60000) // Executa a cada 60 segundos
    @Transactional
    public void processScheduledChanges() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("Checking for scheduled price changes at {}", now);

        // Buscar mudanças programadas para agora ou no passado que ainda não foram executadas
        List<ScheduledPriceChange> pendingChanges =
                scheduledPriceChangeRepository.findByScheduledAtLessThanEqualAndIsExecutedFalse(now);

        if (pendingChanges.isEmpty()) {
            return;
        }

        log.info("Found {} scheduled price changes to process", pendingChanges.size());

        for (ScheduledPriceChange change : pendingChanges) {
            try {
                processChange(change);
            } catch (Exception e) {
                log.error("Failed to process scheduled change {}: {}",
                        change.getId(), e.getMessage());
                change.setExecutionResult("ERROR: " + e.getMessage());
                scheduledPriceChangeRepository.save(change);
            }
        }
    }

    /**
     * Processa uma mudança específica
     */
    private void processChange(ScheduledPriceChange change) {
        LocalDateTime now = LocalDateTime.now();

        try {
            if (change.getApplyToAllTickets()) {
                // Aplicar a todos os tickets do evento
                List<EventTicket> tickets = eventTicketRepository
                        .findByEventId(change.getPricingStrategy().getEvent().getId());

                int updated = 0;
                for (EventTicket ticket : tickets) {
                    BigDecimal newPrice = calculateNewPrice(ticket, change);
                    if (!newPrice.equals(ticket.getCurrentPrice())) {
                        ticket.updatePrice(newPrice,
                                "Scheduled change: " + change.getChangeType(),
                                "SCHEDULED",
                                change.getPricingStrategy().getId());
                        eventTicketRepository.save(ticket);
                        updated++;
                    }
                }

                change.setExecutionResult("SUCCESS: Updated " + updated + " tickets");

            } else if (change.getEventTicket() != null) {
                // Aplicar a um ticket específico
                EventTicket ticket = change.getEventTicket();
                BigDecimal newPrice = calculateNewPrice(ticket, change);

                if (!newPrice.equals(ticket.getCurrentPrice())) {
                    ticket.updatePrice(newPrice,
                            "Scheduled change: " + change.getChangeType(),
                            "SCHEDULED",
                            change.getPricingStrategy().getId());
                    eventTicketRepository.save(ticket);
                    change.setExecutionResult("SUCCESS: Updated ticket " + ticket.getId());
                } else {
                    change.setExecutionResult("SUCCESS: No change needed");
                }
            }

            change.setIsExecuted(true);
            change.setExecutedAt(now);

            log.info("Processed scheduled change {}: {}", change.getId(), change.getExecutionResult());

        } catch (Exception e) {
            change.setExecutionResult("FAILED: " + e.getMessage());
            throw e;
        }

        scheduledPriceChangeRepository.save(change);
    }

    /**
     * Calcula o novo preço baseado no tipo de mudança
     */
    private BigDecimal calculateNewPrice(EventTicket ticket, ScheduledPriceChange change) {
        BigDecimal currentPrice = ticket.getCurrentPrice();

        try {
            if (change.getChangeType() == null) {
                return currentPrice;
            }

            BigDecimal newPrice = switch (change.getChangeType()) {
                case PERCENTAGE_INCREASE -> {
                    validateChangeValue(change);
                    BigDecimal percentage = change.getChangeValue()
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    yield currentPrice.multiply(BigDecimal.ONE.add(percentage));
                }

                case PERCENTAGE_DECREASE -> {
                    validateChangeValue(change);
                    BigDecimal percentage = change.getChangeValue()
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    yield currentPrice.multiply(BigDecimal.ONE.subtract(percentage));
                }

                case FIXED_INCREASE -> {
                    validateChangeValue(change);
                    yield currentPrice.add(change.getChangeValue());
                }

                case FIXED_DECREASE -> {
                    validateChangeValue(change);
                    yield currentPrice.subtract(change.getChangeValue());
                }

                case SET_PRICE -> {
                    if (change.getNewPrice() == null) {
                        throw new IllegalArgumentException("New price is required for SET_PRICE");
                    }
                    yield change.getNewPrice();
                }

                default -> currentPrice;
            };

            // Aplicar limites da estratégia se existirem
            newPrice = applyStrategyPriceLimits(newPrice, change.getPricingStrategy());

            // Garantir que não seja negativo e arredondar
            return newPrice.max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            log.error("Error calculating new price for change {}: {}", change.getId(), e.getMessage());
            return currentPrice;
        }
    }

    /**
     * Aplica os limites de preço da estratégia
     */
    private BigDecimal applyStrategyPriceLimits(BigDecimal price, PricingStrategy strategy) {
        if (strategy == null) return price;

        BigDecimal result = price;

        if (strategy.getMinPrice() != null && result.compareTo(strategy.getMinPrice()) < 0) {
            result = strategy.getMinPrice();
            log.debug("Applied min price limit: {}", result);
        }

        if (strategy.getMaxPrice() != null && result.compareTo(strategy.getMaxPrice()) > 0) {
            result = strategy.getMaxPrice();
            log.debug("Applied max price limit: {}", result);
        }

        return result;
    }
    private void validateChangeValue(ScheduledPriceChange change) {
        if (change.getChangeValue() == null) {
            throw new IllegalArgumentException(
                    "Change value is required for type: " + change.getChangeType()
            );
        }
    }    /**
     * Processa mudanças agendadas para o próximo período (execução mais pesada)
     */
    @Scheduled(cron = "0 0 1 * * *") // Todo dia à 1:00 AM
    @Transactional
    public void processDailySummary() {
        log.info("Running daily scheduled changes summary");

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<ScheduledPriceChange> executedYesterday =
                scheduledPriceChangeRepository.findByExecutedAtAfter(yesterday);

        long success = executedYesterday.stream()
                .filter(c -> c.getExecutionResult() != null &&
                        c.getExecutionResult().startsWith("SUCCESS"))
                .count();

        long failed = executedYesterday.size() - success;

        log.info("Daily summary: {} processed, {} successful, {} failed",
                executedYesterday.size(), success, failed);
    }
}