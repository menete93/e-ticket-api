package mz.co.mozbuy.e_ticket.event.core.factory;

import org.springframework.stereotype.Component;
import mz.co.mozbuy.e_ticket.event.core.dto.BulkStrategyAssignmentDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class StrategyFactory {

    /**
     * Converte as atribuições mínimas em DTOs completos com todas as configurações
     */
    public List<PricingStrategyRequestDTO> buildFullRequests(BulkStrategyAssignmentDTO request) {
        List<PricingStrategyRequestDTO> fullRequests = new ArrayList<>();

        for (BulkStrategyAssignmentDTO.StrategyAssignment assignment : request.getStrategyAssignments()) {
            // Para cada categoria alvo, cria um DTO completo
            for (TicketCategory category : assignment.getTargetCategories()) {
                PricingStrategyRequestDTO fullRequest = buildStrategyRequest(
                        request.getEventId(),
                        assignment.getStrategyId(),
                        category
                );
                fullRequests.add(fullRequest);
            }
        }

        return fullRequests;
    }

    /**
     * Constrói o DTO completo baseado no tipo de estratégia
     */
    private PricingStrategyRequestDTO buildStrategyRequest(Long eventId, String strategyId, TicketCategory category) {
        PricingStrategyType type = PricingStrategyType.valueOf(strategyId);

        switch (type) {
            case EARLY_BIRD:
                return createEarlyBirdStrategy(eventId, category);

            case LAST_MINUTE:
                return createLastMinuteStrategy(eventId, category);

            case LOYALTY_DISCOUNT:
                return createLoyaltyStrategy(eventId, category);

            case FIRST_BUYER:
                return createFirstBuyerStrategy(eventId, category);

            case GROUP_DISCOUNT:
                return createGroupDiscountStrategy(eventId, category);

            case VOLUME_BASED:
                return createVolumeBasedStrategy(eventId, category);

            case BUNDLE_DISCOUNT:
                return createBundleStrategy(eventId, category);

            case TIERED_PRICING:
                return createTieredPricingStrategy(eventId, category);

            case FLASH_SALE:
                return createFlashSaleStrategy(eventId, category);

            case WEEKEND_SPECIAL:
                return createWeekendSpecialStrategy(eventId, category);

            case DEMAND_BASED:
                return createDemandBasedStrategy(eventId, category);

            case CATEGORY_SPECIFIC:
                return createCategorySpecificStrategy(eventId, category);

            default:
                return createDefaultStrategy(eventId, type, category);
        }
    }

    // ==================== MÉTODOS DE CRIAÇÃO ====================

    private PricingStrategyRequestDTO createEarlyBirdStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Early Bird - " + category)
                .strategyType(PricingStrategyType.EARLY_BIRD)
                .eventId(eventId)
                .specificCategory(category)
                .daysBeforeEventStart(30)
                .daysBeforeEventEnd(7)
                .percentageAdjustment(new BigDecimal("-20")) // 20% de desconto
                .priority(1)
                .autoApply(true)
                .description("Desconto para compras antecipadas")
                .build();
    }

    private PricingStrategyRequestDTO createLastMinuteStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Last Minute - " + category)
                .strategyType(PricingStrategyType.LAST_MINUTE)
                .eventId(eventId)
                .specificCategory(category)
                .daysBeforeEventEnd(3)
                .percentageAdjustment(new BigDecimal("-30")) // 30% de desconto
                .priority(2)
                .autoApply(true)
                .description("Desconto de última hora")
                .build();
    }

    private PricingStrategyRequestDTO createLoyaltyStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Loyalty Discount - " + category)
                .strategyType(PricingStrategyType.LOYALTY_DISCOUNT)
                .eventId(eventId)
                .specificCategory(category)
                .loyaltyTier("SILVER") // Default
                .percentageAdjustment(new BigDecimal("-15")) // 15% de desconto
                .priority(3)
                .autoApply(true)
                .description("Desconto para clientes fiéis")
                .build();
    }

    private PricingStrategyRequestDTO createFirstBuyerStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("First Time Buyer - " + category)
                .strategyType(PricingStrategyType.FIRST_BUYER)
                .eventId(eventId)
                .specificCategory(category)
                .firstTimeBuyerOnly(true)
                .percentageAdjustment(new BigDecimal("-10")) // 10% de desconto
                .priority(4)
                .autoApply(true)
                .description("Desconto para primeira compra")
                .build();
    }

    private PricingStrategyRequestDTO createGroupDiscountStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Group Discount - " + category)
                .strategyType(PricingStrategyType.GROUP_DISCOUNT)
                .eventId(eventId)
                .specificCategory(category)
                .minGroupSize(5) // Mínimo 5 pessoas
                .groupDiscountPercentage(new BigDecimal("10")) // 10% de desconto
                .percentageAdjustment(new BigDecimal("-10"))
                .priority(5)
                .autoApply(true)
                .description("Desconto para grupos")
                .build();
    }

    private PricingStrategyRequestDTO createVolumeBasedStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Volume Based - " + category)
                .strategyType(PricingStrategyType.VOLUME_BASED)
                .eventId(eventId)
                .specificCategory(category)
                .minPurchases(5) // Mínimo 5 compras anteriores
                .percentageAdjustment(new BigDecimal("-12")) // 12% de desconto
                .priority(6)
                .autoApply(true)
                .description("Desconto baseado em volume de compras")
                .build();
    }

    private PricingStrategyRequestDTO createBundleStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Bundle Discount - " + category)
                .strategyType(PricingStrategyType.BUNDLE_DISCOUNT)
                .eventId(eventId)
                .specificCategory(category)
                .bundleDiscountPercentage(new BigDecimal("15")) // 15% de desconto
                .percentageAdjustment(new BigDecimal("-15"))
                .priority(7)
                .autoApply(true)
                .description("Desconto para pacotes de ingressos")
                .build();
    }

    private PricingStrategyRequestDTO createTieredPricingStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Tiered Pricing - " + category)
                .strategyType(PricingStrategyType.TIERED_PRICING)
                .eventId(eventId)
                .specificCategory(category)
                .minPrice(new BigDecimal("500")) // Preço mínimo
                .maxPrice(new BigDecimal("2000")) // Preço máximo
                .priority(8)
                .autoApply(false)
                .description("Preços por níveis")
                .build();
    }

    private PricingStrategyRequestDTO createFlashSaleStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Flash Sale - " + category)
                .strategyType(PricingStrategyType.FLASH_SALE)
                .eventId(eventId)
                .specificCategory(category)
                .percentageAdjustment(new BigDecimal("-25")) // 25% de desconto
                .priority(9)
                .autoApply(true)
                .description("Promoção relâmpago")
                // As datas serão definidas dinamicamente
                .build();
    }

    private PricingStrategyRequestDTO createWeekendSpecialStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Weekend Special - " + category)
                .strategyType(PricingStrategyType.WEEKEND_SPECIAL)
                .eventId(eventId)
                .specificCategory(category)
                .percentageAdjustment(new BigDecimal("-18")) // 18% de desconto
                .priority(10)
                .autoApply(true)
                .description("Promoção de fim de semana")
                .build();
    }

    private PricingStrategyRequestDTO createDemandBasedStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Demand Based - " + category)
                .strategyType(PricingStrategyType.DEMAND_BASED)
                .eventId(eventId)
                .specificCategory(category)
                .salesThreshold(70) // Quando 70% vendidos
                .multiplier(new BigDecimal("1.2")) // Aumenta 20%
                .priority(11)
                .autoApply(true)
                .description("Preço baseado na demanda")
                .build();
    }

    private PricingStrategyRequestDTO createCategorySpecificStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Category Specific - " + category)
                .strategyType(PricingStrategyType.CATEGORY_SPECIFIC)
                .eventId(eventId)
                .specificCategory(category)
                .fixedAdjustment(new BigDecimal("100")) // Aumento fixo de 100
                .priority(12)
                .autoApply(true)
                .description("Estratégia específica por categoria")
                .build();
    }

    private PricingStrategyRequestDTO createDefaultStrategy(Long eventId, PricingStrategyType type, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name(type.name() + " - " + category)
                .strategyType(type)
                .eventId(eventId)
                .specificCategory(category)
                .priority(99)
                .autoApply(false)
                .description("Estratégia padrão")
                .build();
    }
}