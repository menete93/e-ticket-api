package mz.co.mozbuy.e_ticket.event.core.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.BulkStrategyAssignmentDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkStrategyService {

    private final PricingStrategyService strategyRouterService;

    /**
     * Processa o payload mínimo do front-end e cria as estratégias
     * usando os helpers methods e validações existentes
     */
    @Transactional
    public List<PricingStrategyResponseDTO> createStrategiesFromAssignment(BulkStrategyAssignmentDTO request) {
        List<PricingStrategyResponseDTO> responses = new ArrayList<>();

        for (BulkStrategyAssignmentDTO.StrategyAssignment assignment : request.getStrategyAssignments()) {
            PricingStrategyType strategyType = PricingStrategyType.valueOf(assignment.getStrategyId());

            for (TicketCategory category : assignment.getTargetCategories()) {
                // PASSO 1: Usar helper method para criar DTO com configurações padrão
                PricingStrategyRequestDTO strategyRequest = createStrategyUsingHelper(
                        request.getEventId(),
                        strategyType,
                        category
                );

                // PASSO 2: Log para debug (opcional)
                log.debug("Estratégia criada via helper: {} para categoria {}",
                        strategyType, category);

                // PASSO 3: VALIDAÇÃO - o helper method garante dados corretos,
                // mas validamos por segurança (defensive programming)
                if (!strategyRequest.isValid()) {
                    throw new ValidationException(
                            String.format("Estratégia %s para categoria %s é inválida após configuração",
                                    strategyType, category)
                    );
                }

                // PASSO 4: Delegar para o router service existente
                PricingStrategyResponseDTO response = strategyRouterService.createStrategyByType(strategyRequest);
                responses.add(response);
            }
        }

        log.info("✅ {} estratégias criadas com sucesso para o evento {}",
                responses.size(), request.getEventId());

        return responses;
    }

    /**
     * Factory method que usa os helpers existentes para criar o DTO completo
     */
    private PricingStrategyRequestDTO createStrategyUsingHelper(
            Long eventId,
            PricingStrategyType strategyType,
            TicketCategory category) {

        switch (strategyType) {
            case LOYALTY_DISCOUNT:
            case LOYALTY:
                // Para estratégias de fidelidade, podemos ter diferentes tiers
                return createLoyaltyStrategy(eventId, category, "GOLD"); // Default GOLD

            case TIERED_PRICING:
                return createTieredPricingStrategy(eventId, category);

            case EARLY_BIRD:
                return PricingStrategyRequestDTO.createEarlyBirdStrategy(
                        eventId,
                        30, // 30 dias antes
                        new BigDecimal("20") // 20% desconto
                );

            case LAST_MINUTE:
                return PricingStrategyRequestDTO.builder()
                        .name("Last Minute - " + category)
                        .strategyType(PricingStrategyType.LAST_MINUTE)
                        .eventId(eventId)
                        .specificCategory(category)
                        .daysBeforeEventEnd(3)
                        .percentageAdjustment(new BigDecimal("-30"))
                        .priority(2)
                        .autoApply(true)
                        .description("Desconto de última hora")
                        .build();

            case GROUP_DISCOUNT:
                return PricingStrategyRequestDTO.createGroupDiscountStrategy(
                        eventId,
                        5, // mínimo 5 pessoas
                        new BigDecimal("10") // 10% desconto
                );

            case FIRST_BUYER:
                return PricingStrategyRequestDTO.createFirstBuyerStrategy(eventId);

            case VOLUME_BASED:
                return PricingStrategyRequestDTO.createVolumeBasedStrategy(eventId);

            case BUNDLE:
            case BUNDLE_DISCOUNT:
                // Bundle precisa de lista de tickets - talvez vir de outra fonte
                return createBundleStrategy(eventId, category);

            case FLASH_SALE:
                return PricingStrategyRequestDTO.builder()
                        .name("Flash Sale - " + category)
                        .strategyType(PricingStrategyType.FLASH_SALE)
                        .eventId(eventId)
                        .specificCategory(category)
                        .percentageAdjustment(new BigDecimal("-25"))
                        .priority(9)
                        .autoApply(true)
                        .description("Promoção relâmpago")
                        .build();

            case WEEKEND_SPECIAL:
                return PricingStrategyRequestDTO.builder()
                        .name("Weekend Special - " + category)
                        .strategyType(PricingStrategyType.WEEKEND_SPECIAL)
                        .eventId(eventId)
                        .specificCategory(category)
                        .percentageAdjustment(new BigDecimal("-18"))
                        .priority(10)
                        .autoApply(true)
                        .description("Promoção de fim de semana")
                        .build();

            case DEMAND_BASED:
                return PricingStrategyRequestDTO.builder()
                        .name("Demand Based - " + category)
                        .strategyType(PricingStrategyType.DEMAND_BASED)
                        .eventId(eventId)
                        .specificCategory(category)
                        .salesThreshold(70)
                        .multiplier(new BigDecimal("1.2"))
                        .priority(11)
                        .autoApply(true)
                        .description("Preço baseado na demanda")
                        .build();

            case CATEGORY_SPECIFIC:
                return PricingStrategyRequestDTO.builder()
                        .name("Category Specific - " + category)
                        .strategyType(PricingStrategyType.CATEGORY_SPECIFIC)
                        .eventId(eventId)
                        .specificCategory(category)
                        .fixedAdjustment(new BigDecimal("100"))
                        .priority(12)
                        .autoApply(true)
                        .description("Estratégia específica por categoria")
                        .build();

            default:
                // Fallback para estratégias sem helper específico
                return PricingStrategyRequestDTO.builder()
                        .name(strategyType.name() + " - " + category)
                        .strategyType(strategyType)
                        .eventId(eventId)
                        .specificCategory(category)
                        .priority(99)
                        .autoApply(false)
                        .description("Estratégia padrão")
                        .build();
        }
    }

    /**
     * Estratégia de fidelidade com configuração por tier
     */
    private PricingStrategyRequestDTO createLoyaltyStrategy(Long eventId, TicketCategory category, String tier) {
        switch (tier) {
            case "PLATINUM":
                return PricingStrategyRequestDTO.createPlatinumStrategy(eventId);
            case "GOLD":
                return PricingStrategyRequestDTO.createGoldStrategy(eventId);
            case "SILVER":
                return PricingStrategyRequestDTO.builder()
                        .name("SILVER Member 10% OFF - " + category)
                        .strategyType(PricingStrategyType.LOYALTY_DISCOUNT)
                        .eventId(eventId)
                        .specificCategory(category)
                        .loyaltyTier("SILVER")
                        .percentageAdjustment(new BigDecimal("-10"))
                        .priority(3)
                        .autoApply(true)
                        .description("Desconto para clientes SILVER")
                        .build();
            default:
                return PricingStrategyRequestDTO.builder()
                        .name("Loyalty Discount - " + category)
                        .strategyType(PricingStrategyType.LOYALTY_DISCOUNT)
                        .eventId(eventId)
                        .specificCategory(category)
                        .loyaltyTier("BRONZE")
                        .percentageAdjustment(new BigDecimal("-5"))
                        .priority(4)
                        .autoApply(true)
                        .description("Desconto para clientes fiéis")
                        .build();
        }
    }

    /**
     * Estratégia de preços por níveis
     */
    private PricingStrategyRequestDTO createTieredPricingStrategy(Long eventId, TicketCategory category) {
        return PricingStrategyRequestDTO.builder()
                .name("Tiered Pricing - " + category)
                .strategyType(PricingStrategyType.TIERED_PRICING)
                .eventId(eventId)
                .specificCategory(category)
                .minPrice(new BigDecimal("500"))
                .maxPrice(new BigDecimal("2000"))
                .priority(8)
                .autoApply(false)
                .description("Preços por níveis")
                .build();
    }

    /**
     * Estratégia de bundle (precisa de lógica adicional para buscar tickets)
     */
    private PricingStrategyRequestDTO createBundleStrategy(Long eventId, TicketCategory category) {
        // Aqui você pode buscar tickets do evento ou definir um bundle padrão
        // Por enquanto, retorna um esqueleto que precisará ser completado
        return PricingStrategyRequestDTO.builder()
                .name("Bundle Discount - " + category)
                .strategyType(PricingStrategyType.BUNDLE_DISCOUNT)
                .eventId(eventId)
                .specificCategory(category)
                .bundleDiscountPercentage(new BigDecimal("15"))
                .percentageAdjustment(new BigDecimal("-15"))
                .priority(7)
                .autoApply(true)
                .description("Desconto para pacotes de ingressos")
                .build();
    }
}