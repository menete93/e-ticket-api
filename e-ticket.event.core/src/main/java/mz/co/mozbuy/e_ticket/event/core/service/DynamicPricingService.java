package mz.co.mozbuy.e_ticket.event.core.service;

import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.model.CustomerPurchaseHistory;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import mz.co.mozbuy.e_ticket.event.core.repository.CustomerPurchaseHistoryRepository;
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
    private final LoyaltyService loyaltyService;
    private final CustomerPurchaseHistoryRepository purchaseHistoryRepository;

    // ==================== MÉTODOS PÚBLICOS ====================

    public BigDecimal calculatePrice(EventTicket ticket, PricingStrategy strategy) {
        return calculatePrice(ticket, strategy, null);
    }

    public BigDecimal calculatePrice(EventTicket ticket, PricingStrategy strategy, Long userId) {
        if (!strategy.isActive()) {
            return ticket.getCurrentPrice();
        }

        return switch (strategy.getStrategyType()) {
            // 📊 Demanda e Vendas
            case DEMAND_BASED -> calculateDemandBasedPrice(ticket, strategy);
            case TIERED_PRICING -> calculateTieredPricingPrice(ticket, strategy);

            // ⏰ Baseado em Tempo
            case EARLY_BIRD -> calculateEarlyBirdPrice(ticket, strategy, userId);
            case LAST_MINUTE -> calculateLastMinutePrice(ticket, strategy, userId);
            case FLASH_SALE -> calculateFlashSalePrice(ticket, strategy);
            case WEEKEND_SPECIAL -> calculateWeekendSpecialPrice(ticket, strategy);

            // 👥 Descontos Especiais
            case GROUP_DISCOUNT -> calculateGroupPrice(ticket, strategy);
            case BUNDLE_DISCOUNT -> calculateBundlePrice(ticket, strategy);
            case BUNDLE -> calculateBundlePrice(ticket, strategy);

            // 💝 Fidelidade
            case LOYALTY_DISCOUNT -> calculateLoyaltyDiscountPrice(ticket, strategy, userId);
            case LOYALTY -> calculateLoyaltyPrice(ticket, strategy, userId);
            case FIRST_BUYER -> calculateFirstBuyerPrice(ticket, strategy, userId);
            case VOLUME_BASED -> calculateVolumeBasedPrice(ticket, strategy, userId);

            // 🎟️ Categoria
            case CATEGORY_SPECIFIC -> calculateCategorySpecificPrice(ticket, strategy);

            default -> {
                log.warn("Unknown strategy type: {}, using current price", strategy.getStrategyType());
                yield ticket.getCurrentPrice();
            }
        };
    }

    // ==================== MÉTODOS AGENDADOS ====================

    @Scheduled(cron = "0 0 6,12,18 * * *")
    @Transactional
    public void applyDynamicPricing() {
        log.info("Starting scheduled dynamic pricing application at {}", LocalDateTime.now());
        List<PricingStrategy> activeStrategies = pricingStrategyRepository.findActiveAndAutoApplied();

        for (PricingStrategy strategy : activeStrategies) {
            try {
                applyPricingStrategy(strategy);
                strategy.setLastAppliedAt(LocalDateTime.now());
                pricingStrategyRepository.save(strategy);
                log.info("Applied pricing strategy: {} to event: {}", strategy.getStrategyName(), strategy.getEvent().getId());
            } catch (Exception e) {
                log.error("Failed to apply pricing strategy: {}", strategy.getId(), e);
            }
        }
    }

    @Transactional
    public void applyPricingStrategy(PricingStrategy strategy) {
        List<EventTicket> tickets = eventTicketRepository.findByEventId(strategy.getEvent().getId());
        int appliedCount = 0;

        for (EventTicket ticket : tickets) {
            if (ticket.getState().equals(LifeCycleState.ACTIVE) && ticket.isSalesPeriodActive()) {
                BigDecimal newPrice = calculatePrice(ticket, strategy);

                if (!newPrice.equals(ticket.getCurrentPrice())) {
                    ticket.updatePrice(newPrice,
                            "Dynamic pricing - " + strategy.getStrategyType(),
                            strategy.getStrategyType().name(),
                            strategy.getId());
                    eventTicketRepository.save(ticket);
                    appliedCount++;
                    log.debug("Updated ticket {} price: {} -> {}", ticket.getId(), ticket.getCurrentPrice(), newPrice);
                }
            }
        }
        log.info("Applied strategy {} to {} tickets", strategy.getStrategyName(), appliedCount);
    }

    // ==================== MÉTODOS DE CÁLCULO ====================

    /**
     * DEMAND_BASED: Preço baseado na demanda (porcentagem de vendas)
     */
    /**
     * DEMAND_BASED: Preço baseado na demanda (porcentagem de vendas)
     * Versão melhorada com logs e validações
     */
    private BigDecimal calculateDemandBasedPrice(EventTicket ticket, PricingStrategy strategy) {
        try {
            double soldPercentage = ticket.getSoldPercentage();
            log.debug("Calculating DEMAND_BASED price for ticket {}: sold={}%",
                    ticket.getId(), soldPercentage);

            // Preço base
            BigDecimal basePrice = getBasePrice(ticket, strategy);
            log.debug("Base price: {}", basePrice);

            // Multiplier (fator de intensidade)
            BigDecimal multiplier = strategy.getMultiplier() != null ?
                    strategy.getMultiplier() : BigDecimal.ONE;

            // Fórmula: preço = basePrice * (1 + (%vendido * multiplier / 100))
            // Quanto mais vendido, maior o preço
            double demandFactor = 1.0 + (soldPercentage * multiplier.doubleValue() / 100.0);
            BigDecimal price = basePrice.multiply(BigDecimal.valueOf(demandFactor));

            log.debug("After demand multiplier ({}): {}", demandFactor, price);

            // Aplicar ajustes adicionais
            price = applyAdditionalAdjustments(price, strategy);

            // Aplicar limites
            BigDecimal finalPrice = applyPriceLimits(price, strategy);

            log.debug("Final DEMAND_BASED price: {} (from {} with {}% sold)",
                    finalPrice, basePrice, soldPercentage);

            return finalPrice;

        } catch (Exception e) {
            log.error("Error calculating DEMAND_BASED price: {}", e.getMessage());
            return ticket.getCurrentPrice();
        }
    }

    /**
     * Obtém o preço base prioritário
     */
    private BigDecimal getBasePrice(EventTicket ticket, PricingStrategy strategy) {
        // Para estratégias de demanda/tiragem, o mais lógico é usar o preço original
        // como referência, pois ele representa o valor "base" do ingresso

        BigDecimal basePrice;

        // 1. Tenta usar o preço original do ticket
        if (ticket.getOriginalPrice() != null) {
            basePrice = ticket.getOriginalPrice();
            log.debug("Using original price as base: {}", basePrice);
        }
        // 2. Se não tem original, usa o current (pode já ter sido alterado)
        else {
            basePrice = ticket.getCurrentPrice();
            log.debug("Using current price as base (no original): {}", basePrice);
        }

        return basePrice;
    }

    /**
     * Aplica ajustes percentuais e fixos adicionais
     */
    private BigDecimal applyAdditionalAdjustments(BigDecimal price, PricingStrategy strategy) {
        BigDecimal result = price;

        // Ajuste percentual (ex: +10%, -15%)
        if (strategy.getPercentageAdjustment() != null) {
            BigDecimal adjustmentFactor = BigDecimal.ONE.add(
                    strategy.getPercentageAdjustment().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
            result = result.multiply(adjustmentFactor);
            log.debug("After percentage adjustment ({}%): {}",
                    strategy.getPercentageAdjustment(), result);
        }

        // Ajuste fixo (ex: +R$5, -R$10)
        if (strategy.getFixedAdjustment() != null) {
            result = result.add(strategy.getFixedAdjustment());
            log.debug("After fixed adjustment ({}): {}",
                    strategy.getFixedAdjustment(), result);
        }

        return result;
    }
    /**
     * TIERED_PRICING: Preço por níveis de venda (mais granular)
     */
    /**
     * TIERED_PRICING: Preço por níveis de venda
     * Versão otimizada com sua implementação original + melhorias
     */
    private BigDecimal calculateTieredPricingPrice(EventTicket ticket, PricingStrategy strategy) {
        double soldPercentage = ticket.getSoldPercentage();

        // Usa o método que criamos
        BigDecimal basePrice = getBasePrice(ticket, strategy);

        log.debug("Calculating TIERED_PRICING: sold={}%, basePrice={}", soldPercentage, basePrice);

        BigDecimal price;

        if (soldPercentage >= 90) {
            price = basePrice.multiply(new BigDecimal("1.5"));
        } else if (soldPercentage >= 75) {
            price = basePrice.multiply(new BigDecimal("1.35"));
        } else if (soldPercentage >= 60) {
            price = basePrice.multiply(new BigDecimal("1.2"));
        } else if (soldPercentage >= 45) {
            price = basePrice.multiply(new BigDecimal("1.1"));
        } else if (soldPercentage >= 30) {
            price = basePrice;
        } else if (soldPercentage >= 15) {
            price = basePrice.multiply(new BigDecimal("0.95"));
        } else {
            price = basePrice.multiply(new BigDecimal("0.85"));
        }

        // Aplicar ajustes adicionais da estratégia
        if (strategy.getPercentageAdjustment() != null) {
            BigDecimal factor = BigDecimal.ONE.add(
                    strategy.getPercentageAdjustment().divide(BigDecimal.valueOf(100))
            );
            price = price.multiply(factor);
        }

        if (strategy.getFixedAdjustment() != null) {
            price = price.add(strategy.getFixedAdjustment());
        }

        return applyPriceLimits(price, strategy);
    }    /**
     * EARLY_BIRD: Desconto para compras antecipadas
     */
    private BigDecimal calculateEarlyBirdPrice(EventTicket ticket, PricingStrategy strategy, Long userId) {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), ticket.getEvent().getEventDate());

        Integer startDays = strategy.getDaysBeforeEventStart() != null ?
                strategy.getDaysBeforeEventStart() : 60;
        Integer endDays = strategy.getDaysBeforeEventEnd() != null ?
                strategy.getDaysBeforeEventEnd() : 30;

        if (daysUntilEvent >= endDays && daysUntilEvent <= startDays) {
            BigDecimal discount = strategy.getPercentageAdjustment() != null ?
                    strategy.getPercentageAdjustment() : new BigDecimal("-15"); // -15% padrão

            BigDecimal discountFactor = BigDecimal.ONE.add(discount.divide(BigDecimal.valueOf(100)));
            BigDecimal newPrice = ticket.getOriginalPrice().multiply(discountFactor);

            return applyPriceLimits(newPrice, strategy);
        }

        return ticket.getCurrentPrice();
    }

    /**
     * LAST_MINUTE: Aumento na última hora
     */
    private BigDecimal calculateLastMinutePrice(EventTicket ticket, PricingStrategy strategy, Long userId) {
        long hoursUntilEvent = ChronoUnit.HOURS.between(LocalDateTime.now(), ticket.getEvent().getEventDate());
        Integer lastMinuteHours = strategy.getDaysBeforeEventStart() != null ?
                strategy.getDaysBeforeEventStart() * 24 : 48; // 48 horas padrão

        if (hoursUntilEvent <= lastMinuteHours) {
            BigDecimal increase = strategy.getPercentageAdjustment() != null ?
                    strategy.getPercentageAdjustment() : new BigDecimal("20"); // +20% padrão

            BigDecimal increaseFactor = BigDecimal.ONE.add(increase.divide(BigDecimal.valueOf(100)));
            BigDecimal newPrice = ticket.getCurrentPrice().multiply(increaseFactor);

            return applyPriceLimits(newPrice, strategy);
        }

        return ticket.getCurrentPrice();
    }

    /**
     * FLASH_SALE: Promoção relâmpago
     */
    private BigDecimal calculateFlashSalePrice(EventTicket ticket, PricingStrategy strategy) {
        if (!strategy.isCurrentlyApplicable()) {
            return ticket.getCurrentPrice();
        }

        BigDecimal discount = strategy.getPercentageAdjustment() != null ?
                strategy.getPercentageAdjustment() : new BigDecimal("-25"); // -25% padrão

        BigDecimal discountFactor = BigDecimal.ONE.add(discount.divide(BigDecimal.valueOf(100)));
        BigDecimal newPrice = ticket.getCurrentPrice().multiply(discountFactor);

        return applyPriceLimits(newPrice, strategy);
    }

    /**
     * WEEKEND_SPECIAL: Preço especial para fins de semana
     */
    private BigDecimal calculateWeekendSpecialPrice(EventTicket ticket, PricingStrategy strategy) {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();

        if (dayOfWeek >= 6) { // Sábado (6) e Domingo (7)
            BigDecimal adjustment = strategy.getPercentageAdjustment() != null ?
                    strategy.getPercentageAdjustment() : new BigDecimal("-10"); // -10% padrão

            BigDecimal factor = BigDecimal.ONE.add(adjustment.divide(BigDecimal.valueOf(100)));
            BigDecimal newPrice = ticket.getCurrentPrice().multiply(factor);

            return applyPriceLimits(newPrice, strategy);
        }

        return ticket.getCurrentPrice();
    }

    /**
     * GROUP_DISCOUNT: Desconto para grupos
     */
    private BigDecimal calculateGroupPrice(EventTicket ticket, PricingStrategy strategy) {
        if (strategy.getGroupDiscountPercentage() == null) {
            return ticket.getCurrentPrice();
        }

        BigDecimal discountPercentage = strategy.getGroupDiscountPercentage();
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
                discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );

        BigDecimal newPrice = ticket.getCurrentPrice().multiply(discountFactor);
        return applyPriceLimits(newPrice, strategy);
    }

    /**
     * BUNDLE_DISCOUNT: Desconto para compra de múltiplos ingressos
     */
    private BigDecimal calculateBundlePrice(EventTicket ticket, PricingStrategy strategy) {
        // Nota: Este desconto é melhor aplicado no checkout com quantidade
        // Aqui retornamos o preço unitário com possível desconto base
        if (strategy.getPercentageAdjustment() != null) {
            BigDecimal factor = BigDecimal.ONE.add(
                    strategy.getPercentageAdjustment().divide(BigDecimal.valueOf(100))
            );
            return applyPriceLimits(ticket.getCurrentPrice().multiply(factor), strategy);
        }
        return ticket.getCurrentPrice();
    }

    /**
     * LOYALTY_DISCOUNT: Desconto para clientes frequentes (versão específica)
     */
    private BigDecimal calculateLoyaltyDiscountPrice(EventTicket ticket, PricingStrategy strategy, Long userId) {
        if (userId == null) return ticket.getCurrentPrice();

        if (!loyaltyService.isEligibleForLoyaltyStrategy(userId, strategy)) {
            return ticket.getCurrentPrice();
        }

        CustomerPurchaseHistory history = purchaseHistoryRepository.findByUserId(userId).orElse(null);
        if (history == null) return ticket.getCurrentPrice();

        BigDecimal currentPrice = ticket.getCurrentPrice();
        BigDecimal discount = BigDecimal.ZERO;

        // Desconto baseado no tier do cliente
        switch (history.getLoyaltyTier()) {
            case "PLATINUM":
                discount = currentPrice.multiply(new BigDecimal("0.20")); // 20% off
                break;
            case "GOLD":
                discount = currentPrice.multiply(new BigDecimal("0.15")); // 15% off
                break;
            case "SILVER":
                discount = currentPrice.multiply(new BigDecimal("0.10")); // 10% off
                break;
            case "BRONZE":
                discount = currentPrice.multiply(new BigDecimal("0.05")); // 5% off
                break;
            default:
                discount = BigDecimal.ZERO;
        }

        BigDecimal newPrice = currentPrice.subtract(discount).max(BigDecimal.ZERO);
        return applyPriceLimits(newPrice, strategy);
    }

    /**
     * LOYALTY: Desconto para clientes fiéis (versão configurável)
     */
    private BigDecimal calculateLoyaltyPrice(EventTicket ticket, PricingStrategy strategy, Long userId) {
        if (userId == null) return ticket.getCurrentPrice();

        if (!loyaltyService.isEligibleForLoyaltyStrategy(userId, strategy)) {
            return ticket.getCurrentPrice();
        }

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal currentPrice = ticket.getCurrentPrice();

        if (strategy.getPercentageAdjustment() != null) {
            discount = currentPrice.multiply(
                    strategy.getPercentageAdjustment().abs().divide(BigDecimal.valueOf(100))
            );
        } else {
            discount = loyaltyService.getLoyaltyDiscount(userId, currentPrice);
        }

        BigDecimal newPrice = currentPrice.subtract(discount).max(BigDecimal.ZERO);
        return applyPriceLimits(newPrice, strategy);
    }

    /**
     * FIRST_BUYER: Desconto para primeira compra
     */
    private BigDecimal calculateFirstBuyerPrice(EventTicket ticket, PricingStrategy strategy, Long userId) {
        if (userId == null) return ticket.getCurrentPrice();

        CustomerPurchaseHistory history = purchaseHistoryRepository.findByUserId(userId).orElse(null);

        if (history == null || history.getTotalPurchases() == 0) {
            BigDecimal discount = strategy.getPercentageAdjustment() != null ?
                    strategy.getPercentageAdjustment() : new BigDecimal("-15"); // -15% padrão

            BigDecimal discountFactor = BigDecimal.ONE.add(discount.divide(BigDecimal.valueOf(100)));
            BigDecimal newPrice = ticket.getCurrentPrice().multiply(discountFactor);

            return applyPriceLimits(newPrice, strategy);
        }

        return ticket.getCurrentPrice();
    }

    /**
     * VOLUME_BASED: Desconto baseado em volume de compras
     */
    private BigDecimal calculateVolumeBasedPrice(EventTicket ticket, PricingStrategy strategy, Long userId) {
        if (userId == null) return ticket.getCurrentPrice();

        CustomerPurchaseHistory history = purchaseHistoryRepository.findByUserId(userId).orElse(null);
        if (history == null) return ticket.getCurrentPrice();

        BigDecimal currentPrice = ticket.getCurrentPrice();
        BigDecimal discount = BigDecimal.ZERO;

        int purchases = history.getTotalPurchases();

        if (purchases >= 20) {
            discount = currentPrice.multiply(new BigDecimal("0.25")); // 25% off
        } else if (purchases >= 15) {
            discount = currentPrice.multiply(new BigDecimal("0.20")); // 20% off
        } else if (purchases >= 10) {
            discount = currentPrice.multiply(new BigDecimal("0.15")); // 15% off
        } else if (purchases >= 5) {
            discount = currentPrice.multiply(new BigDecimal("0.10")); // 10% off
        } else if (purchases >= 3) {
            discount = currentPrice.multiply(new BigDecimal("0.05")); // 5% off
        }

        BigDecimal newPrice = currentPrice.subtract(discount).max(BigDecimal.ZERO);
        return applyPriceLimits(newPrice, strategy);
    }

    /**
     * CATEGORY_SPECIFIC: Estratégia específica por categoria
     */
    private BigDecimal calculateCategorySpecificPrice(EventTicket ticket, PricingStrategy strategy) {
        BigDecimal price = ticket.getCurrentPrice();

        if (strategy.getPercentageAdjustment() != null) {
            BigDecimal factor = BigDecimal.ONE.add(
                    strategy.getPercentageAdjustment().divide(BigDecimal.valueOf(100))
            );
            price = price.multiply(factor);
        }

        if (strategy.getFixedAdjustment() != null) {
            price = price.add(strategy.getFixedAdjustment());
        }

        return applyPriceLimits(price, strategy);
    }

    /**
     * Aplica limites mínimo e máximo de preço
     */
    private BigDecimal applyPriceLimits(BigDecimal price, PricingStrategy strategy) {
        BigDecimal result = price;

        if (strategy.getMinPrice() != null && result.compareTo(strategy.getMinPrice()) < 0) {
            result = strategy.getMinPrice();
        }

        if (strategy.getMaxPrice() != null && result.compareTo(strategy.getMaxPrice()) > 0) {
            result = strategy.getMaxPrice();
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Previsão de preço futuro
     */
    public BigDecimal predictFuturePrice(Long ticketId, Long userId, LocalDateTime futureDate) {
        EventTicket ticket = eventTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        PricingStrategy topStrategy = pricingStrategyRepository
                .findTopByEventIdAndStateOrderByPriorityDesc(ticket.getEvent().getId(),LifeCycleState.ACTIVE)
                .orElse(null);

        if (topStrategy == null) {
            return ticket.getCurrentPrice();
        }

        return calculatePrice(ticket, topStrategy, userId);
    }
}