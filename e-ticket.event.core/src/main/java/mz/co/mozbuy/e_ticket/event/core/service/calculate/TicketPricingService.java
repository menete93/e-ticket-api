package mz.co.mozbuy.e_ticket.event.core.service.calculate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceBreakdownItemDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceCalculationRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceCalculationResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.model.CustomerPurchaseHistory;
import mz.co.mozbuy.e_ticket.event.core.repository.TicketSaleRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.PricingStrategyRepository;
import mz.co.mozbuy.e_ticket.event.core.service.LoyaltyService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPricingService {

    private final TicketSaleRepository ticketSaleRepository;
    private final PricingStrategyRepository strategyRepository;
    private final LoyaltyService loyaltyService;

    /**
     * Calcula o preço final com todas as estratégias aplicáveis
     */
    public PriceCalculationResponseDTO calculatePrice(PriceCalculationRequestDTO request) {
        log.info("📊 Calculando preço para {} tickets do evento: {}",
                request.getTicketQuantities().size(), request.getEventId());

        // Buscar histórico do cliente (se logado)
        CustomerPurchaseHistory customerHistory = null;
        if (request.getUserId() != null) {
            Optional<CustomerPurchaseHistory> historyOpt = loyaltyService.getCustomerHistory(request.getUserId());
            if (historyOpt.isPresent()) {
                customerHistory = historyOpt.get();
                log.debug("Cliente: tier={}, compras={}, total gasto={}",
                        customerHistory.getLoyaltyTier(),
                        customerHistory.getTotalPurchases(),
                        customerHistory.getTotalSpent());
            }
        }

        List<PriceBreakdownItemDTO> breakdown = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // Buscar todos os TicketSale pelos IDs
        List<Long> ticketIds = new ArrayList<>(request.getTicketQuantities().keySet());
        List<TicketSale> tickets = ticketSaleRepository.findAllById(ticketIds);

        // Mapear TicketSale por ID para fácil acesso
        Map<Long, TicketSale> ticketSaleMap = tickets.stream()
                .collect(Collectors.toMap(TicketSale::getId, t -> t));

        // 🔥 IMPORTANTE: Mapear EventTicket por ID para facilitar acesso
        Map<Long, EventTicket> eventTicketMap = new HashMap<>();
        for (TicketSale ticketSale : tickets) {
            EventTicket eventTicket = ticketSale.getTicket();
            if (eventTicket != null) {
                eventTicketMap.put(ticketSale.getId(), eventTicket);
            }
        }

        // Calcular subtotal base usando o preço ORIGINAL do EventTicket
        for (Map.Entry<Long, Integer> entry : request.getTicketQuantities().entrySet()) {
            Long ticketSaleId = entry.getKey();
            Integer quantity = entry.getValue();

            TicketSale ticketSale = ticketSaleMap.get(ticketSaleId);
            EventTicket eventTicket = eventTicketMap.get(ticketSaleId);

            if (ticketSale == null || eventTicket == null || quantity <= 0) continue;

            // ✅ CORREÇÃO: Usar o originalPrice do EventTicket (preço base definido pelo organizador)
            BigDecimal originalPrice = eventTicket.getOriginalPrice();

            // 🔥 FALLBACK: Se não tiver originalPrice no EventTicket, usa o currentPrice
            if (originalPrice == null) {
                originalPrice = eventTicket.getCurrentPrice();
                log.debug("Ticket {} sem originalPrice, usando currentPrice: {}",
                        eventTicket.getId(), originalPrice);
            }

            BigDecimal ticketSubtotal = originalPrice.multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(ticketSubtotal);

            breakdown.add(PriceBreakdownItemDTO.builder()
                    .type("TICKET")
                    .ticketId(ticketSaleId)
                    .ticketName(eventTicket.getTicketName())
                    .category(eventTicket.getCategory())
                    .basePrice(originalPrice)
                    .quantity(quantity)
                    .subtotal(ticketSubtotal)
                    .build());
        }

        // Buscar estratégias ativas para o evento
        List<PricingStrategy> strategies = strategyRepository
                .findByEventIdAndStateOrderByPriorityDesc(
                        request.getEventId(),
                        LifeCycleState.ACTIVE
                );

        log.debug("🔍 Encontradas {} estratégias ativas para o evento {}",
                strategies.size(), request.getEventId());

        // Aplicar estratégias automáticas
        PriceCalculationResponseDTO response = applyAutomaticStrategies(
                request,
                customerHistory,
                subtotal,
                breakdown,
                strategies,
                eventTicketMap,  // 👈 Passar mapa de EventTicket
                ticketSaleMap);   // 👈 Passar mapa de TicketSale

        // Calcular total final
        response.setFinalPrice(calculateFinalPrice(response));
        response.setTotalSavings(calculateTotalSavings(subtotal, response.getFinalPrice()));

        log.info("✅ Preço calculado: Subtotal={}, Final={}, Economia={}",
                subtotal, response.getFinalPrice(), response.getTotalSavings());

        return response;
    }

    private PriceCalculationResponseDTO applyAutomaticStrategies(
            PriceCalculationRequestDTO request,
            CustomerPurchaseHistory customerHistory,
            BigDecimal subtotal,
            List<PriceBreakdownItemDTO> breakdown,
            List<PricingStrategy> strategies,
            Map<Long, EventTicket> eventTicketMap,
            Map<Long, TicketSale> ticketSaleMap) {

        PriceCalculationResponseDTO response = new PriceCalculationResponseDTO();
        response.setSubtotal(subtotal);
        response.setBreakdown(breakdown);
        response.setDiscountedPrice(subtotal);
        response.setAppliedStrategies(new ArrayList<>());

        if (strategies.isEmpty()) {
            return response;
        }

        // Mapa para controlar estratégias já aplicadas por categoria
        Map<TicketCategory, Boolean> categoryStrategyApplied = new HashMap<>();
        BigDecimal currentPrice = subtotal;
        List<PriceBreakdownItemDTO> discounts = new ArrayList<>();

        for (PricingStrategy strategy : strategies) {
            // Validações básicas
            if (!isStrategyActive(strategy)) continue;
            if (!isCurrentlyApplicable(strategy)) continue;
            if (!strategy.isAutoApply()) continue;

            // Verificar se já aplicou estratégia para esta categoria
            if (strategy.getSpecificCategory() != null) {
                if (categoryStrategyApplied.containsKey(strategy.getSpecificCategory())) {
                    continue;
                }
            }

            // Verificar elegibilidade do cliente
            if (!isStrategyApplicableToCustomer(strategy, customerHistory, request)) {
                continue;
            }

            // Verificar se a estratégia se aplica aos tickets selecionados
            if (!isStrategyApplicableToTickets(strategy, request.getTicketQuantities().keySet(), eventTicketMap)) {
                continue;
            }

            log.debug("✅ Estratégia aplicável: {}", strategy.getStrategyName());

            // Calcular desconto
            BigDecimal discount = calculateStrategyDiscount(
                    strategy,
                    currentPrice,
                    getTotalQuantity(request),
                    customerHistory,
                    eventTicketMap,
                    ticketSaleMap
            );

            if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                currentPrice = currentPrice.subtract(discount);

                if (strategy.getSpecificCategory() != null) {
                    categoryStrategyApplied.put(strategy.getSpecificCategory(), true);
                }

                discounts.add(PriceBreakdownItemDTO.builder()
                        .type("STRATEGY")
                        .name(strategy.getStrategyName())
                        .description(strategy.getDescription())
                        .discountValue(discount)
                        .build());

                log.debug("💰 Desconto de {} aplicado", discount);
            }
        }

        response.setDiscountedPrice(currentPrice);
        response.setAppliedStrategies(discounts);

        return response;
    }

    /**
     * Verifica se a estratégia se aplica aos tickets selecionados
     */
    private boolean isStrategyApplicableToTickets(
            PricingStrategy strategy,
            Set<Long> ticketSaleIds,
            Map<Long, EventTicket> eventTicketMap) {

        // Se não tem categoria específica, aplica a todos
        if (strategy.getSpecificCategory() == null) return true;

        // Verificar se algum ticket é da categoria específica
        return ticketSaleIds.stream()
                .map(eventTicketMap::get)
                .filter(Objects::nonNull)
                .map(EventTicket::getCategory)
                .anyMatch(category -> category == strategy.getSpecificCategory());
    }

    /**
     * Verifica se a estratégia se aplica ao cliente
     */
    private boolean isStrategyApplicableToCustomer(
            PricingStrategy strategy,
            CustomerPurchaseHistory customerHistory,
            PriceCalculationRequestDTO request) {

        // Estratégias de fidelidade
        if (isLoyaltyStrategy(strategy)) {
            return loyaltyService.isEligibleForLoyaltyStrategy(request.getUserId(), strategy);
        }

        // Estratégias de grupo
        if (strategy.getMinGroupSize() != null) {
            int totalQuantity = getTotalQuantity(request);
            if (totalQuantity < strategy.getMinGroupSize()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifica se é estratégia de fidelidade
     */
    private boolean isLoyaltyStrategy(PricingStrategy strategy) {
        return strategy.getLoyaltyTier() != null ||
                strategy.isFirstTimeBuyerOnly() ||
                strategy.isRepeatBuyerOnly() ||
                strategy.getMinPurchases() != null ||
                strategy.getMinTotalSpent() != null;
    }

    /**
     * Verifica se a estratégia está ativa
     */
    private boolean isStrategyActive(PricingStrategy strategy) {
        return strategy.getState() == LifeCycleState.ACTIVE;
    }

    /**
     * Verifica se a estratégia é aplicável no momento
     */
    private boolean isCurrentlyApplicable(PricingStrategy strategy) {
        LocalDateTime now = LocalDateTime.now();

        if (strategy.getCustomStartDate() != null && strategy.getCustomEndDate() != null) {
            return !now.isBefore(strategy.getCustomStartDate()) &&
                    !now.isAfter(strategy.getCustomEndDate());
        }

        return true;
    }

    /**
     * Calcula o desconto baseado na estratégia
     */
    private BigDecimal calculateStrategyDiscount(
            PricingStrategy strategy,
            BigDecimal currentPrice,
            int totalQuantity,
            CustomerPurchaseHistory customerHistory,
            Map<Long, EventTicket> eventTicketMap,
            Map<Long, TicketSale> ticketSaleMap) {

        // Desconto de fidelidade
        if (isLoyaltyStrategy(strategy) && customerHistory != null) {
            return loyaltyService.getLoyaltyDiscount(customerHistory.getUserId(), currentPrice);
        }

        // Desconto percentual
        if (strategy.getPercentageAdjustment() != null &&
                strategy.getPercentageAdjustment().compareTo(BigDecimal.ZERO) < 0) {

            BigDecimal percent = strategy.getPercentageAdjustment().abs();
            return currentPrice
                    .multiply(percent)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        // Desconto fixo
        if (strategy.getFixedAdjustment() != null &&
                strategy.getFixedAdjustment().compareTo(BigDecimal.ZERO) < 0) {

            return strategy.getFixedAdjustment()
                    .abs()
                    .multiply(BigDecimal.valueOf(totalQuantity));
        }

        // Multiplicador
        if (strategy.getMultiplier() != null &&
                strategy.getMultiplier().compareTo(BigDecimal.ONE) < 0) {

            BigDecimal newPrice = currentPrice.multiply(strategy.getMultiplier());
            return currentPrice.subtract(newPrice);
        }

        // Desconto em grupo
        if (strategy.getGroupDiscountPercentage() != null &&
                strategy.getMinGroupSize() != null &&
                totalQuantity >= strategy.getMinGroupSize()) {

            return currentPrice
                    .multiply(strategy.getGroupDiscountPercentage())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     *  Obtém o preço original de um ticket (agora apenas do EventTicket)
     */
    private BigDecimal getOriginalPrice(Long ticketSaleId, Map<Long, EventTicket> eventTicketMap) {
        EventTicket eventTicket = eventTicketMap.get(ticketSaleId);
        if (eventTicket != null) {
            if (eventTicket.getOriginalPrice() != null) {
                return eventTicket.getOriginalPrice();
            }
            return eventTicket.getCurrentPrice();
        }
        return BigDecimal.ZERO;
    }

    private int getTotalQuantity(PriceCalculationRequestDTO request) {
        return request.getTicketQuantities().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private BigDecimal calculateFinalPrice(PriceCalculationResponseDTO response) {
        return response.getDiscountedPrice() != null ?
                response.getDiscountedPrice() : response.getSubtotal();
    }

    private BigDecimal calculateTotalSavings(BigDecimal subtotal, BigDecimal finalPrice) {
        return subtotal.subtract(finalPrice);
    }
}