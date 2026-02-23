package mz.co.mozbuy.e_ticket.event.core.service;


import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import mz.co.mozbuy.e_ticket.event.core.repository.EventTicketRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.PricingStrategyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicPricingService {

    private final EventTicketRepository eventTicketRepository;
    private final PricingStrategyRepository pricingStrategyRepository;

    private BigDecimal calculateDynamicPrice(EventTicket ticket, PricingStrategy strategy) {
        BigDecimal currentPrice = ticket.getCurrentPrice();
        String strategyType = strategy.getStrategyType();

        if ("DYNAMIC".equals(strategyType)) {
            return calculateDemandBasedPrice(ticket, strategy);
        } else if ("TIME_BASED".equals(strategyType)) {
            return calculateTimeBasedPrice(ticket, strategy);
        } else if ("TIERED".equals(strategyType)) {
            return calculateTieredPrice(ticket, strategy);
        } else if ("GROUP_DISCOUNT".equals(strategyType)) {
            return calculateGroupPrice(ticket, strategy);
        } else {
            return currentPrice;
        }
    }

    /**
     * Calcula preço baseado na demanda (porcentagem de vendas)
     */
    private BigDecimal calculateDemandBasedPrice(EventTicket ticket, PricingStrategy strategy) {
        double soldPercentage = ticket.getSoldPercentage();
        BigDecimal basePrice = strategy.getBasePrice() != null ? strategy.getBasePrice() : ticket.getOriginalPrice();
        BigDecimal multiplier = strategy.getDemandMultiplier() != null ? strategy.getDemandMultiplier() : BigDecimal.ONE;

        // Aumentar preço baseado na demanda (porcentagem vendida)
        double demandFactor = 1.0 + (soldPercentage / 100.0 * multiplier.doubleValue());
        BigDecimal newPrice = basePrice.multiply(BigDecimal.valueOf(demandFactor));

        return applyPriceLimits(newPrice, strategy);
    }

    /**
     * Calcula preço baseado no tempo até o evento
     */
    private BigDecimal calculateTimeBasedPrice(EventTicket ticket, PricingStrategy strategy) {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), ticket.getEvent().getEventDate());

        if (strategy.getTimeBasedIncreaseDays() != null && daysUntilEvent <= strategy.getTimeBasedIncreaseDays()) {
            BigDecimal increasePercentage = strategy.getTimeBasedIncreasePercentage() != null ?
                    strategy.getTimeBasedIncreasePercentage() : new BigDecimal("10");

            // Aumento percentual baseado na proximidade do evento
            BigDecimal increaseFactor = BigDecimal.ONE.add(increasePercentage.divide(BigDecimal.valueOf(100)));
            BigDecimal newPrice = ticket.getCurrentPrice().multiply(increaseFactor);

            return applyPriceLimits(newPrice, strategy);
        }

        return ticket.getCurrentPrice();
    }

    /**
     * Calcula preço usando estratégia de tiers (níveis)
     */
    private BigDecimal calculateTieredPrice(EventTicket ticket, PricingStrategy strategy) {
        double soldPercentage = ticket.getSoldPercentage();
        BigDecimal basePrice = strategy.getBasePrice() != null ? strategy.getBasePrice() : ticket.getOriginalPrice();

        // Definir tiers baseados na porcentagem vendida
        if (soldPercentage >= 80) {
            // Tier 4: 80-100% vendido - preço máximo
            return applyPriceLimits(basePrice.multiply(new BigDecimal("1.4")), strategy);
        } else if (soldPercentage >= 60) {
            // Tier 3: 60-79% vendido - preço alto
            return applyPriceLimits(basePrice.multiply(new BigDecimal("1.25")), strategy);
        } else if (soldPercentage >= 40) {
            // Tier 2: 40-59% vendido - preço médio
            return applyPriceLimits(basePrice.multiply(new BigDecimal("1.1")), strategy);
        } else if (soldPercentage >= 20) {
            // Tier 1: 20-39% vendido - preço base
            return basePrice;
        } else {
            // Tier 0: 0-19% vendido - preço promocional
            return applyPriceLimits(basePrice.multiply(new BigDecimal("0.9")), strategy);
        }
    }

    /**
     * Calcula preço com desconto para grupos
     */
    private BigDecimal calculateGroupPrice(EventTicket ticket, PricingStrategy strategy) {
        BigDecimal currentPrice = ticket.getCurrentPrice();

        if (strategy.getGroupDiscountPercentage() != null && strategy.getGroupSizeThreshold() != null) {
            // Aplicar desconto progressivo baseado no tamanho do grupo
            BigDecimal discountPercentage = strategy.getGroupDiscountPercentage();

            // Exemplo: 5% de desconto para grupos, pode ser ajustado
            BigDecimal discountFactor = BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100)));
            BigDecimal newPrice = currentPrice.multiply(discountFactor);

            return applyPriceLimits(newPrice, strategy);
        }

        return currentPrice;
    }

    /**
     * Calcula preço baseado em limiares de demanda específicos
     */
    private BigDecimal calculateDemandBasedPriceWithThresholds(EventTicket ticket, PricingStrategy strategy) {
        double soldPercentage = ticket.getSoldPercentage();
        BigDecimal currentPrice = ticket.getCurrentPrice();

        if (strategy.getDemandThresholdPercentage() != null && strategy.getPriceIncreasePercentage() != null) {
            // Se atingiu o limiar de demanda, aplicar aumento
            if (soldPercentage >= strategy.getDemandThresholdPercentage().doubleValue()) {
                BigDecimal increaseFactor = BigDecimal.ONE.add(
                        strategy.getPriceIncreasePercentage().divide(BigDecimal.valueOf(100))
                );
                return applyPriceLimits(currentPrice.multiply(increaseFactor), strategy);
            }
        }

        return currentPrice;
    }

    /**
     * Aplica limites mínimo e máximo de preço
     */
    private BigDecimal applyPriceLimits(BigDecimal price, PricingStrategy strategy) {
        BigDecimal result = price;

        // Aplicar limite mínimo
        if (strategy.getMinPrice() != null && result.compareTo(strategy.getMinPrice()) < 0) {
            result = strategy.getMinPrice();
        }

        // Aplicar limite máximo
        if (strategy.getMaxPrice() != null && result.compareTo(strategy.getMaxPrice()) > 0) {
            result = strategy.getMaxPrice();
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Aplica estratégias de pricing dinâmico
     */
    @Scheduled(cron = "0 0 6,12,18 * * *") // Executa 3x ao dia (6h, 12h, 18h)
    @Transactional
    public void applyDynamicPricing() {
        List<PricingStrategy> activeStrategies =
                pricingStrategyRepository.findActiveAndAutoApplied();

        for (PricingStrategy strategy : activeStrategies) {
            try {
                applyPricingStrategy(strategy);
                strategy.setLastAppliedAt(LocalDateTime.now());
                pricingStrategyRepository.save(strategy);

                log.info("Applied pricing strategy: {} to event: {}",
                        strategy.getStrategyName(), strategy.getEvent().getId());
            } catch (Exception e) {
                log.error("Failed to apply pricing strategy: {}", strategy.getId(), e);
            }
        }
    }

    private void applyPricingStrategy(PricingStrategy strategy) {
        List<EventTicket> tickets = eventTicketRepository.findByEventId(strategy.getEvent().getId());

        for (EventTicket ticket : tickets) {
            if (ticket.getLifeCycleState().equals(LifeCycleState.ACTIVE) && ticket.isSalesPeriodActive()) {
                BigDecimal newPrice = calculateDynamicPrice(ticket, strategy);

                // Aplicar mudança se for diferente do preço atual
                if (!newPrice.equals(ticket.getCurrentPrice())) {
                    ticket.updatePrice(newPrice, "Dynamic pricing - " + strategy.getStrategyType());
                    eventTicketRepository.save(ticket);

                    log.debug("Updated ticket {} price: {} -> {}",
                            ticket.getId(), ticket.getCurrentPrice(), newPrice);
                }
            }
        }
    }
}