package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.*;
import mz.co.mozbuy.e_ticket.event.core.enums.PriceAdjustmentType;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.model.*;
import mz.co.mozbuy.e_ticket.event.core.repository.*;
import mz.co.mozbuy.e_ticket.event.core.service.pricing.PricingHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    // Repositórios
    private final PricingStrategyRepository pricingStrategyRepository;
    private final EventTicketRepository eventTicketRepository;
    private final ScheduledPriceChangeRepository scheduledPriceChangeRepository;
    private final TicketPriceHistoryRepository ticketPriceHistoryRepository;
    private final EventRepository eventRepository;

    // Adicione este método no PricingService para acessar o PricingStrategyService
    // Services (delegar responsabilidades)
    @Getter
    private final PricingStrategyService pricingStrategyService;
    private final DynamicPricingService dynamicPricingService;
    private final PricingHistoryService pricingHistoryService;
    private final LoyaltyService loyaltyService;

    // ==================== ESTRATÉGIAS DE PRECIFICAÇÃO ====================

    /**
     * Criar nova estratégia de precificação
     */
    @Transactional
    public PricingStrategyResponseDTO createPricingStrategy(PricingStrategyRequestDTO requestDTO) {
        // Validações básicas
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + requestDTO.getEventId()));

        // Verificar se já existe estratégia com mesmo nome
        if (pricingStrategyRepository.existsByEventIdAndStrategyNameIgnoreCase(
                requestDTO.getEventId(), requestDTO.getName())) {
            throw new RuntimeException("Pricing strategy with name '" + requestDTO.getName() + "' already exists for this event");
        }

        // Validação de limites
        if (requestDTO.getMinPrice() != null && requestDTO.getMaxPrice() != null
                && requestDTO.getMinPrice().compareTo(requestDTO.getMaxPrice()) > 0) {
            throw new RuntimeException("Minimum price cannot be greater than maximum price");
        }

        // Validar a estratégia
        if (!requestDTO.isValid()) {
            throw new RuntimeException("Invalid strategy configuration");
        }

        // Delegar para o PricingStrategyService
        return pricingStrategyService.createStrategy(requestDTO);
    }

    /**
     * Atualizar estratégia existente
     */
    @Transactional
    public PricingStrategyResponseDTO updatePricingStrategy(Long id, PricingStrategyRequestDTO requestDTO) {
        return pricingStrategyService.updateStrategy(id, requestDTO);
    }

    /**
     * Deletar/desativar estratégia
     */
    @Transactional
    public void deletePricingStrategy(Long id) {
        pricingStrategyService.deleteStrategy(id);
    }

    /**
     * Buscar estratégia por ID
     */
    @Transactional(readOnly = true)
    public PricingStrategyResponseDTO getPricingStrategy(Long id) {
        return pricingStrategyService.getStrategy(id);
    }

    /**
     * Listar todas estratégias ativas de um evento
     */
    @Transactional(readOnly = true)
    public List<PricingStrategyResponseDTO> getStrategiesByEvent(Long eventId) {
        return pricingStrategyService.getStrategiesByEvent(eventId);
    }

    /**
     * Listar todas estratégias (admin)
     */
    @Transactional(readOnly = true)
    public List<PricingStrategyResponseDTO> findAllStrategies() {
        return pricingStrategyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== APLICAÇÃO DE ESTRATÉGIAS ====================

    /**
     * Aplicar estratégia manualmente
     */
    @Transactional
    public StrategyApplicationResultDTO applyStrategyManually(Long strategyId) {
        return pricingStrategyService.applyStrategyManually(strategyId);
    }

    /**
     * Aplicar estratégia a um ticket específico (com userId para fidelidade)
     */
    @Transactional
    public BigDecimal applyStrategyToTicket(Long strategyId, Long ticketId, Long userId) {
        PricingStrategy strategy = pricingStrategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));

        EventTicket ticket = eventTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        BigDecimal newPrice = dynamicPricingService.calculatePrice(ticket, strategy, userId);

        if (!newPrice.equals(ticket.getCurrentPrice())) {
            ticket.updatePrice(newPrice,
                    "Applied strategy: " + strategy.getStrategyName(),
                    strategy.getStrategyType().name(),
                    strategyId);
            eventTicketRepository.save(ticket);
        }

        return newPrice;
    }

    /**
     * Aplicar estratégias automáticas a um evento
     */
    @Transactional
    public List<StrategyApplicationResultDTO> applyAutoStrategiesToEvent(Long eventId) {
        return pricingStrategyService.applyAutoStrategiesToEvent(eventId);
    }


    // ==================== MUDANÇAS PROGRAMADAS ====================

    /**
     * Agendar mudança de preço
     */
    @Transactional
    public ScheduledPriceChangeResponseDTO schedulePriceChange(ScheduledPriceChangeRequestDTO requestDTO) {
        PricingStrategy pricingStrategy = pricingStrategyRepository.findById(requestDTO.getPricingStrategyId())
                .orElseThrow(() -> new RuntimeException("Pricing strategy not found"));

        EventTicket eventTicket = null;
        if (requestDTO.getEventTicketId() != null) {
            eventTicket = eventTicketRepository.findById(requestDTO.getEventTicketId())
                    .orElseThrow(() -> new RuntimeException("Event ticket not found"));
        }

        ScheduledPriceChange scheduledChange = new ScheduledPriceChange();
        scheduledChange.setPricingStrategy(pricingStrategy);
        scheduledChange.setEventTicket(eventTicket);
        scheduledChange.setChangeType(requestDTO.getChangeType());
        scheduledChange.setChangeValue(requestDTO.getChangeValue());
        scheduledChange.setNewPrice(requestDTO.getNewPrice());
        scheduledChange.setScheduledAt(requestDTO.getScheduledAt());
        scheduledChange.setApplyToAllTickets(requestDTO.getApplyToAllTickets());
        scheduledChange.setIsExecuted(false);

        ScheduledPriceChange savedChange = scheduledPriceChangeRepository.save(scheduledChange);
        log.info("Scheduled price change for strategy: {} at {}",
                pricingStrategy.getStrategyName(), requestDTO.getScheduledAt());

        return convertToScheduledChangeDTO(savedChange);
    }

    /**
     * Listar mudanças programadas de um evento
     */
    @Transactional(readOnly = true)
    public List<ScheduledPriceChangeResponseDTO> getScheduledChangesByEvent(Long eventId) {
        return scheduledPriceChangeRepository.findByEventId(eventId).stream()
                .map(this::convertToScheduledChangeDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cancelar mudança programada
     */
    @Transactional
    public void cancelScheduledChange(Long changeId) {
        ScheduledPriceChange change = scheduledPriceChangeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("Scheduled change not found"));

        if (change.getIsExecuted()) {
            throw new RuntimeException("Cannot cancel an already executed change");
        }

        scheduledPriceChangeRepository.delete(change);
        log.info("Cancelled scheduled change: {}", changeId);
    }

    // ==================== HISTÓRICO DE PREÇOS ====================

    /**
     * Buscar histórico de um ticket
     */
    @Transactional(readOnly = true)
    public List<TicketPriceHistoryDTO> getPriceHistoryByTicket(Long ticketId) {
        return pricingHistoryService.getHistoryByTicket(ticketId);
    }

    /**
     * Buscar histórico de um evento
     */
    @Transactional(readOnly = true)
    public List<TicketPriceHistoryDTO> getPriceHistoryByEvent(Long eventId) {
        return pricingHistoryService.getHistoryByEvent(eventId);
    }

    /**
     * Buscar histórico de uma estratégia
     */
    @Transactional(readOnly = true)
    public List<TicketPriceHistoryDTO> getPriceHistoryByStrategy(Long strategyId) {
        return pricingHistoryService.getHistoryByStrategy(strategyId);
    }

    // ==================== PREVISÕES E RELATÓRIOS ====================

    /**
     * Previsão de receita
     */
    public RevenuePredictionDTO predictRevenue(Long eventId) {
        List<EventTicket> tickets = eventTicketRepository.findByEventId(eventId);

        if (tickets.isEmpty()) {
            return RevenuePredictionDTO.builder()
                    .totalExpected(BigDecimal.ZERO)
                    .totalSold(BigDecimal.ZERO)
                    .totalRemaining(BigDecimal.ZERO)
                    .ticketCount(0)
                    .build();
        }

        BigDecimal totalExpected = BigDecimal.ZERO;
        BigDecimal totalSold = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;

        for (EventTicket ticket : tickets) {
            int sold = ticket.getSoldQuantity() != null ? ticket.getSoldQuantity() : 0;
            int capacity = ticket.getTotalQuantity() != null ? ticket.getTotalQuantity() : 0;
            int remaining = capacity - sold;

            BigDecimal currentPrice = ticket.getCurrentPrice() != null ?
                    ticket.getCurrentPrice() : BigDecimal.ZERO;

            // Projeção de receita total
            BigDecimal projected = currentPrice.multiply(BigDecimal.valueOf(capacity));
            totalExpected = totalExpected.add(projected);

            // Receita já realizada
            BigDecimal soldRevenue = currentPrice.multiply(BigDecimal.valueOf(sold));
            totalSold = totalSold.add(soldRevenue);

            // Receita potencial restante
            BigDecimal remainingRevenue = currentPrice.multiply(BigDecimal.valueOf(remaining));
            totalRemaining = totalRemaining.add(remainingRevenue);
        }

        return RevenuePredictionDTO.builder()
                .totalExpected(totalExpected)
                .totalSold(totalSold)
                .totalRemaining(totalRemaining)
                .ticketCount(tickets.size())
                .build();
    }

    /**
     * Prever preço futuro de um ticket
     */
    public BigDecimal predictFuturePrice(Long ticketId, Long userId, LocalDateTime futureDate) {
        return dynamicPricingService.predictFuturePrice(ticketId, userId, futureDate);
    }

    /**
     * Relatório de estratégias mais eficazes
     */
    public List<PricingStrategyEffectivenessDTO> getStrategyEffectivenessReport(Long eventId) {
        List<PricingStrategy> strategies = pricingStrategyRepository.findByEventIdAndLifeCycleStateAfterOrderByPriorityDesc(eventId,LifeCycleState.ACTIVE);

        return strategies.stream()
                .map(s -> PricingStrategyEffectivenessDTO.builder()
                        .strategyId(s.getId())
                        .strategyName(s.getStrategyName())
                        .strategyType(s.getStrategyType())
                        .timesApplied(s.getTimesApplied() != null ? s.getTimesApplied() : 0)
                        .totalDiscountGiven(s.getTotalDiscountGiven() != null ? s.getTotalDiscountGiven() : BigDecimal.ZERO)
                        .totalRevenueGenerated(s.getTotalRevenueGenerated() != null ? s.getTotalRevenueGenerated() : BigDecimal.ZERO)
                        .lastAppliedAt(s.getLastAppliedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS DE CONVERSÃO ====================

    private PricingStrategyResponseDTO convertToDTO(PricingStrategy strategy) {
        return PricingStrategyResponseDTO.builder()
                .id(strategy.getId())
                .name(strategy.getStrategyName())
                .strategyType(strategy.getStrategyType())
                .eventId(strategy.getEvent().getId())
                .eventName(strategy.getEvent().getName())
                .specificCategory(strategy.getSpecificCategory())
                .daysBeforeEventStart(strategy.getDaysBeforeEventStart())
                .daysBeforeEventEnd(strategy.getDaysBeforeEventEnd())
                .customStartDate(strategy.getCustomStartDate())
                .customEndDate(strategy.getCustomEndDate())
                .salesThreshold(strategy.getSalesThreshold())
                .percentageAdjustment(strategy.getPercentageAdjustment())
                .fixedAdjustment(strategy.getFixedAdjustment())
                .multiplier(strategy.getMultiplier())
                .minPrice(strategy.getMinPrice())
                .maxPrice(strategy.getMaxPrice())
                .minGroupSize(strategy.getMinGroupSize())
                .groupDiscountPercentage(strategy.getGroupDiscountPercentage())
                .loyaltyTier(strategy.getLoyaltyTier())
                .minPurchases(strategy.getMinPurchases())
                .minTotalSpent(strategy.getMinTotalSpent())
                .firstTimeBuyerOnly(strategy.isFirstTimeBuyerOnly())
                .repeatBuyerOnly(strategy.isRepeatBuyerOnly())
                .priority(strategy.getPriority())
                .active(strategy.isActive())
                .autoApply(strategy.isAutoApply())
                .lastAppliedAt(strategy.getLastAppliedAt())
                .timesApplied(strategy.getTimesApplied())
                .totalDiscountGiven(strategy.getTotalDiscountGiven())
                .totalRevenueGenerated(strategy.getTotalRevenueGenerated())
                .createdAt(strategy.getCreatedAt())
                .updatedAt(strategy.getUpdatedAt())
                .build();
    }

    private ScheduledPriceChangeResponseDTO convertToScheduledChangeDTO(ScheduledPriceChange change) {
        if (change == null) return null;
        return ScheduledPriceChangeResponseDTO.fromEntity(change);
    }}