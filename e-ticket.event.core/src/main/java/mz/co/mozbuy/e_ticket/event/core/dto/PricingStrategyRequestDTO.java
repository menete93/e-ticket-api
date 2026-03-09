package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingStrategyRequestDTO {
    private String name;
    private PricingStrategyType strategyType;
    private Long eventId;
    private TicketCategory specificCategory; // Opcional

    // ⏰ Configurações temporais
    private Integer daysBeforeEventStart;
    private Integer daysBeforeEventEnd;
    private LocalDateTime customStartDate;
    private LocalDateTime customEndDate;

    // 📊 Configurações de demanda
    private Integer salesThreshold; // Percentual
    private Integer availableTicketsThreshold;

    // 💰 Ajustes de preço (pelo menos um deve ser fornecido)
    private BigDecimal percentageAdjustment; // Ex: -20, +15
    private BigDecimal fixedAdjustment;      // Ex: -10.00, +5.00
    private BigDecimal multiplier;           // Ex: 1.5

    // 🎯 Limites
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // 👥 Regras de grupo
    private Integer minGroupSize;
    private BigDecimal groupDiscountPercentage;

    // 📦 CONFIGURAÇÕES DE BUNDLE (NOVO)
    private List<Long> bundleTicketIds;      // IDs dos tickets no bundle
    private BigDecimal bundleDiscountPercentage; // Desconto específico do bundle

    // 🎟️ REGRAS DE FIDELIDADE
    private String loyaltyTier;              // BRONZE, SILVER, GOLD, PLATINUM
    private Integer minPurchases;            // Mínimo de compras anteriores
    private BigDecimal minTotalSpent;        // Mínimo gasto total
    private boolean firstTimeBuyerOnly;      // Apenas para primeira compra
    private boolean repeatBuyerOnly;         // Apenas para compras repetidas
    private boolean exclusiveToTier;         // Exclusivo para um tier específico
    private String description;              // Descrição da estratégia

    // ⚙️ Configurações gerais
    private Integer priority;
    private boolean autoApply;

    // ==================== MÉTODOS DE VALIDAÇÃO ====================

    /**
     * Validação básica da estratégia
     */
    public boolean isValid() {
        // Validar campos obrigatórios comuns
//        if (name == null || name.trim().isEmpty()) return false;
        if (strategyType == null) return false;
        if (eventId == null) return false;
//        if (priority == null || priority <= 0) return false;

        // Validar se tem pelo menos um ajuste de preço (exceto para bundle que pode ter desconto específico)
//        boolean hasPriceAdjustment = percentageAdjustment != null ||
//                fixedAdjustment != null ||
//                multiplier != null ||
//                (strategyType == PricingStrategyType.BUNDLE && bundleDiscountPercentage != null) ||
//                (strategyType == PricingStrategyType.BUNDLE_DISCOUNT && bundleDiscountPercentage != null);
//
//        if (!hasPriceAdjustment) return false;

        // Validações específicas por tipo
        return validateByStrategyType();
    }

    /**
     * Validações específicas para cada tipo de estratégia
     */
    private boolean validateByStrategyType() {
        if (strategyType == null) return false;

        switch (strategyType) {
            // 🎫 Estratégias baseadas em tempo
            case EARLY_BIRD:
            case LAST_MINUTE:
            case TIME_BASED:
                return validateTimeBasedStrategy();

            // 📈 Estratégias baseadas em demanda
            case DEMAND_BASED:
                return validateDemandBasedStrategy();

            // 👥 Descontos em grupo
            case GROUP_DISCOUNT:
                return validateGroupDiscountStrategy();

            // 🏷️ Estratégias por categoria
            case CATEGORY_SPECIFIC:
                return specificCategory != null;

            // ⚡ Promoções relâmpago
            case FLASH_SALE:
            case WEEKEND_SPECIAL:
                return validateFlashSaleStrategy();

            // 💝 Estratégias de fidelidade
            case LOYALTY:
            case LOYALTY_DISCOUNT:
                return validateLoyaltyStrategy();

            // 📊 Preços por níveis
            case TIERED_PRICING:
                return validateTieredPricingStrategy();

            // 📦 Descontos em pacote
            case BUNDLE:
            case BUNDLE_DISCOUNT:
                return validateBundleStrategy();

            // 🎁 Primeira compra
            case FIRST_BUYER:
                return firstTimeBuyerOnly;

            // 📊 Baseado em volume
            case VOLUME_BASED:
                return minPurchases != null && minPurchases > 0;

            default:
                return true;
        }
    }

    private boolean validateTimeBasedStrategy() {
        return (daysBeforeEventStart != null && daysBeforeEventEnd != null) ||
                (customStartDate != null && customEndDate != null);
    }

    private boolean validateDemandBasedStrategy() {
        return (salesThreshold != null && salesThreshold > 0) ||
                (availableTicketsThreshold != null && availableTicketsThreshold > 0);
    }

    private boolean validateGroupDiscountStrategy() {
        return minGroupSize != null && minGroupSize > 0 &&
                (groupDiscountPercentage != null || percentageAdjustment != null);
    }

    private boolean validateFlashSaleStrategy() {
        return customStartDate != null && customEndDate != null &&
                customEndDate.isAfter(customStartDate);
    }

    private boolean validateLoyaltyStrategy() {
        return loyaltyTier != null ||
                minPurchases != null ||
                minTotalSpent != null ||
                firstTimeBuyerOnly ||
                repeatBuyerOnly;
    }

    private boolean validateTieredPricingStrategy() {
        return minPrice != null && maxPrice != null &&
                minPrice.compareTo(maxPrice) < 0;
    }

    private boolean validateBundleStrategy() {
        return bundleTicketIds != null && !bundleTicketIds.isEmpty() &&
                (bundleDiscountPercentage != null || percentageAdjustment != null);
    }

    // ==================== MÉTODOS HELPER PARA CRIAÇÃO ====================

    /**
     * Cria estratégia PLATINUM
     */
    public static PricingStrategyRequestDTO createPlatinumStrategy(Long eventId) {
        return PricingStrategyRequestDTO.builder()
                .name("PLATINUM Member 20% OFF")
                .strategyType(PricingStrategyType.LOYALTY)
                .eventId(eventId)
                .loyaltyTier("PLATINUM")
                .percentageAdjustment(new BigDecimal("-20"))
                .priority(1)
                .autoApply(true)
                .description("Desconto exclusivo para clientes PLATINUM")
                .build();
    }

    /**
     * Cria estratégia GOLD
     */
    public static PricingStrategyRequestDTO createGoldStrategy(Long eventId) {
        return PricingStrategyRequestDTO.builder()
                .name("GOLD Member 15% OFF")
                .strategyType(PricingStrategyType.LOYALTY)
                .eventId(eventId)
                .loyaltyTier("GOLD")
                .percentageAdjustment(new BigDecimal("-15"))
                .priority(2)
                .autoApply(true)
                .description("Desconto especial para clientes GOLD")
                .build();
    }

    /**
     * Cria estratégia para primeira compra
     */
    public static PricingStrategyRequestDTO createFirstBuyerStrategy(Long eventId) {
        return PricingStrategyRequestDTO.builder()
                .name("First Timer 15% OFF")
                .strategyType(PricingStrategyType.FIRST_BUYER)
                .eventId(eventId)
                .firstTimeBuyerOnly(true)
                .percentageAdjustment(new BigDecimal("-15"))
                .priority(5)
                .autoApply(true)
                .description("Desconto especial para primeira compra")
                .build();
    }

    /**
     * Cria estratégia baseada em volume
     */
    public static PricingStrategyRequestDTO createVolumeBasedStrategy(Long eventId) {
        return PricingStrategyRequestDTO.builder()
                .name("Super Fã 25% OFF")
                .strategyType(PricingStrategyType.VOLUME_BASED)
                .eventId(eventId)
                .minPurchases(10)
                .percentageAdjustment(new BigDecimal("-25"))
                .priority(6)
                .autoApply(true)
                .description("Desconto para clientes com mais de 10 compras")
                .build();
    }

    /**
     * Cria estratégia de bundle
     */
    public static PricingStrategyRequestDTO createBundleStrategy(Long eventId,
                                                                 List<Long> ticketIds,
                                                                 BigDecimal discountPercentage,
                                                                 String name) {
        return PricingStrategyRequestDTO.builder()
                .name(name)
                .strategyType(PricingStrategyType.BUNDLE_DISCOUNT)
                .eventId(eventId)
                .bundleTicketIds(ticketIds)
                .bundleDiscountPercentage(discountPercentage)
                .percentageAdjustment(discountPercentage.negate()) // Para compatibilidade
                .priority(10)
                .autoApply(true)
                .description(String.format("Pacote com %d ingressos com %s%% de desconto",
                        ticketIds.size(), discountPercentage))
                .build();
    }

    /**
     * Cria estratégia Early Bird
     */
    public static PricingStrategyRequestDTO createEarlyBirdStrategy(Long eventId,
                                                                    int daysBefore,
                                                                    BigDecimal discount) {
        return PricingStrategyRequestDTO.builder()
                .name("Early Bird " + daysBefore + " dias")
                .strategyType(PricingStrategyType.EARLY_BIRD)
                .eventId(eventId)
                .daysBeforeEventStart(daysBefore)
                .daysBeforeEventEnd(0)
                .percentageAdjustment(discount.negate())
                .priority(3)
                .autoApply(true)
                .description(String.format("Desconto de %s%% para compras %d dias antes",
                        discount, daysBefore))
                .build();
    }

    /**
     * Cria estratégia de desconto em grupo
     */
    public static PricingStrategyRequestDTO createGroupDiscountStrategy(Long eventId,
                                                                        int minGroupSize,
                                                                        BigDecimal discount) {
        return PricingStrategyRequestDTO.builder()
                .name("Grupo " + minGroupSize + "+ pessoas")
                .strategyType(PricingStrategyType.GROUP_DISCOUNT)
                .eventId(eventId)
                .minGroupSize(minGroupSize)
                .groupDiscountPercentage(discount)
                .percentageAdjustment(discount.negate())
                .priority(4)
                .autoApply(true)
                .description(String.format("Desconto de %s%% para grupos de %d+ pessoas",
                        discount, minGroupSize))
                .build();
    }
}