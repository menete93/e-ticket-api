package mz.co.mozbuy.e_ticket.event.core.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.ScheduledPriceChangeRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.ScheduledPriceChangeResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import mz.co.mozbuy.e_ticket.event.core.model.ScheduledPriceChange;
import mz.co.mozbuy.e_ticket.event.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingStrategyRepository pricingStrategyRepository;
    private final EventTicketRepository eventTicketRepository;
    private final ScheduledPriceChangeRepository scheduledPriceChangeRepository;
    private final PriceAdjustmentRuleRepository priceAdjustmentRuleRepository;
    private final TicketPriceHistoryRepository ticketPriceHistoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    public PricingStrategyResponseDTO createPricingStrategy(PricingStrategyRequestDTO requestDTO) {
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + requestDTO.getEventId()));

        // Verificar se já existe estratégia com mesmo nome
        if (pricingStrategyRepository.existsActiveStrategy(
                requestDTO.getEventId(), requestDTO.getStrategyName())) {
            throw new RuntimeException("Pricing strategy with name '" + requestDTO.getStrategyName() + "' already exists for this event");
        }

        PricingStrategy strategy = new PricingStrategy();
        strategy.setStrategyName(requestDTO.getStrategyName());
        strategy.setStrategyType(requestDTO.getStrategyType());
        strategy.setEvent(event);
        strategy.setBasePrice(requestDTO.getBasePrice());
        strategy.setMinPrice(requestDTO.getMinPrice());
        strategy.setMaxPrice(requestDTO.getMaxPrice());
        strategy.setDemandMultiplier(requestDTO.getDemandMultiplier());
        strategy.setTimeBasedIncreaseDays(requestDTO.getTimeBasedIncreaseDays());
        strategy.setTimeBasedIncreasePercentage(requestDTO.getTimeBasedIncreasePercentage());
        strategy.setGroupSizeThreshold(requestDTO.getGroupSizeThreshold());
        strategy.setGroupDiscountPercentage(requestDTO.getGroupDiscountPercentage());
        strategy.setLifeCycleState(requestDTO.getLifeCycleState());
        strategy.setApplyAutomatically(requestDTO.getApplyAutomatically());

        PricingStrategy savedStrategy = pricingStrategyRepository.save(strategy);
        log.info("Created pricing strategy: {} for event: {}", savedStrategy.getStrategyName(), event.getId());

        return toPricingStrategyDTO(savedStrategy);
    }

    @Transactional
    public ScheduledPriceChangeResponseDTO schedulePriceChange(ScheduledPriceChangeRequestDTO requestDTO) {
        PricingStrategy pricingStrategy = pricingStrategyRepository.findById(requestDTO.getPricingStrategyId())
                .orElseThrow(() -> new RuntimeException("Pricing strategy not found with id: " + requestDTO.getPricingStrategyId()));

        EventTicket eventTicket = null;
        if (requestDTO.getEventTicketId() != null) {
            eventTicket = eventTicketRepository.findById(requestDTO.getEventTicketId())
                    .orElseThrow(() -> new RuntimeException("Event ticket not found with id: " + requestDTO.getEventTicketId()));
        }

        ScheduledPriceChange scheduledChange = new ScheduledPriceChange();
        scheduledChange.setPricingStrategy(pricingStrategy);
        scheduledChange.setEventTicket(eventTicket);
        scheduledChange.setChangeType(requestDTO.getChangeType());
        scheduledChange.setChangeValue(requestDTO.getChangeValue());
        scheduledChange.setNewPrice(requestDTO.getNewPrice());
        scheduledChange.setScheduledAt(requestDTO.getScheduledAt());
        scheduledChange.setApplyToAllTickets(requestDTO.getApplyToAllTickets());

        ScheduledPriceChange savedChange = scheduledPriceChangeRepository.save(scheduledChange);
        log.info("Scheduled price change for strategy: {} at {}",
                pricingStrategy.getStrategyName(), requestDTO.getScheduledAt());

        return toScheduledPriceChangeDTO(savedChange);
    }

    @Transactional
    public void applyPricingStrategy(Long strategyId) {
        PricingStrategy strategy = pricingStrategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Pricing strategy not found with id: " + strategyId));

        if (strategy.getLifeCycleState().equals(LifeCycleState.INACTIVE)) {
            throw new RuntimeException("Pricing strategy is not active");
        }

        List<EventTicket> tickets = eventTicketRepository.findByEventId(strategy.getEvent().getId());

        for (EventTicket ticket : tickets) {
            if (ticket.getLifeCycleState().equals(LifeCycleState.ACTIVE) && ticket.isSalesPeriodActive()) {
                BigDecimal newPrice = calculatePriceWithStrategy(ticket, strategy);

                // Aplicar limites
                if (strategy.getMinPrice() != null && newPrice.compareTo(strategy.getMinPrice()) < 0) {
                    newPrice = strategy.getMinPrice();
                }
                if (strategy.getMaxPrice() != null && newPrice.compareTo(strategy.getMaxPrice()) > 0) {
                    newPrice = strategy.getMaxPrice();
                }

                // Atualizar se diferente
                if (!newPrice.equals(ticket.getCurrentPrice())) {
                    ticket.updatePrice(newPrice, "Manual strategy application: " + strategy.getStrategyName());
                    eventTicketRepository.save(ticket);
                }
            }
        }

        strategy.setLastAppliedAt(LocalDateTime.now());
        pricingStrategyRepository.save(strategy);

        log.info("Applied pricing strategy: {} to event: {}", strategy.getStrategyName(), strategy.getEvent().getId());
    }

    @Transactional
    public void applyDynamicPricingToEvent(Long eventId) {
        List<PricingStrategy> strategies = pricingStrategyRepository.findActiveAutoApplyStrategiesByEventId(eventId);

        for (PricingStrategy strategy : strategies) {
            applyPricingStrategy(strategy.getId());
        }

        log.info("Applied dynamic pricing to event: {}", eventId);
    }

    private BigDecimal calculatePriceWithStrategy(EventTicket ticket, PricingStrategy strategy) {
        // Implementar lógica de cálculo baseada no tipo de estratégia
        // Similar à implementação no DynamicPricingService
        return ticket.getCurrentPrice(); // Placeholder
    }

    private PricingStrategyResponseDTO toPricingStrategyDTO(PricingStrategy strategy) {
        PricingStrategyResponseDTO dto = new PricingStrategyResponseDTO();
        dto.setId(strategy.getId());
        dto.setStrategyName(strategy.getStrategyName());
        dto.setStrategyType(strategy.getStrategyType());
        dto.setEventId(strategy.getEvent().getId());
        dto.setBasePrice(strategy.getBasePrice());
        dto.setMinPrice(strategy.getMinPrice());
        dto.setMaxPrice(strategy.getMaxPrice());
        dto.setDemandMultiplier(strategy.getDemandMultiplier());
        dto.setTimeBasedIncreaseDays(strategy.getTimeBasedIncreaseDays());
        dto.setTimeBasedIncreasePercentage(strategy.getTimeBasedIncreasePercentage());
        dto.setGroupSizeThreshold(strategy.getGroupSizeThreshold());
        dto.setGroupDiscountPercentage(strategy.getGroupDiscountPercentage());
        dto.setLifeCycleState(strategy.getLifeCycleState());
        dto.setApplyAutomatically(strategy.getApplyAutomatically());
        dto.setLastAppliedAt(strategy.getLastAppliedAt());
        dto.setCreatedAt(strategy.getCreatedAt());
        dto.setUpdatedAt(strategy.getUpdatedAt());
        return dto;
    }

    private ScheduledPriceChangeResponseDTO toScheduledPriceChangeDTO(ScheduledPriceChange change) {
        ScheduledPriceChangeResponseDTO dto = new ScheduledPriceChangeResponseDTO();
        dto.setId(change.getId());
        dto.setPricingStrategyId(change.getPricingStrategy().getId());
        dto.setEventTicketId(change.getEventTicket() != null ? change.getEventTicket().getId() : null);
        dto.setChangeType(change.getChangeType());
        dto.setChangeValue(change.getChangeValue());
        dto.setNewPrice(change.getNewPrice());
        dto.setScheduledAt(change.getScheduledAt());
        dto.setApplyToAllTickets(change.getApplyToAllTickets());
        dto.setIsExecuted(change.getIsExecuted());
        dto.setExecutedAt(change.getExecutedAt());
        dto.setExecutionResult(change.getExecutionResult());
        dto.setCreatedAt(change.getCreatedAt());
        return dto;
    }


    public List<PricingStrategyResponseDTO> findAll(){

        return pricingStrategyRepository.findAllActive().stream().
                map(this::toPricingStrategyDTO).collect(Collectors.toList());    }


}