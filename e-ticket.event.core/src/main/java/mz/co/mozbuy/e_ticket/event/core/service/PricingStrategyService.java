package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.*;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.model.*;
import mz.co.mozbuy.e_ticket.event.core.repository.*;
import mz.co.mozbuy.e_ticket.event.core.service.pricing.PricingHistoryService;
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
public class PricingStrategyService {

    private final PricingStrategyRepository strategyRepository;
    private final EventRepository eventRepository;
    private final EventTicketRepository ticketRepository;
    private final TicketPriceHistoryRepository historyRepository;
    private final PricingHistoryService pricingHistoryService;

    // ==================== CRUD OPERATIONS ====================

    @Transactional
    public PricingStrategyResponseDTO createStrategy(PricingStrategyRequestDTO request) {
        // Validar evento
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Validar nome único
        if (strategyRepository.existsByEventIdAndStrategyNameIgnoreCase(event.getId(), request.getName())) {
            throw new RuntimeException("Strategy with name '" + request.getName() + "' already exists for this event");
        }

        // Validar ajustes de preço
        if (!request.isValid()) {
            throw new RuntimeException("At least one price adjustment must be provided");
        }

        // Criar estratégia
        PricingStrategy strategy = PricingStrategy.builder()
                .strategyName(request.getName())
                .strategyType(request.getStrategyType())
                .event(event)
                .specificCategory(request.getSpecificCategory())
                .daysBeforeEventStart(request.getDaysBeforeEventStart())
                .daysBeforeEventEnd(request.getDaysBeforeEventEnd())
                .customStartDate(request.getCustomStartDate())
                .customEndDate(request.getCustomEndDate())
                .salesThreshold(request.getSalesThreshold())
                .availableTicketsThreshold(request.getAvailableTicketsThreshold())
                .percentageAdjustment(request.getPercentageAdjustment())
                .fixedAdjustment(request.getFixedAdjustment())
                .multiplier(request.getMultiplier())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .minGroupSize(request.getMinGroupSize())
                .groupDiscountPercentage(request.getGroupDiscountPercentage())
                .loyaltyTier(request.getLoyaltyTier())
                .minPurchases(request.getMinPurchases())
                .minTotalSpent(request.getMinTotalSpent())
                .firstTimeBuyerOnly(request.isFirstTimeBuyerOnly())
                .repeatBuyerOnly(request.isRepeatBuyerOnly())
                .exclusiveToTier(request.isExclusiveToTier())
                .priority(request.getPriority())
                .autoApply(request.isAutoApply())
                .timesApplied(0)
                .totalDiscountGiven(BigDecimal.ZERO)
                .totalRevenueGenerated(BigDecimal.ZERO)
                .description(request.getDescription())
                .build();

        PricingStrategy saved = strategyRepository.save(strategy);
        log.info("Created pricing strategy: {} for event: {}", saved.getStrategyName(), event.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public PricingStrategyResponseDTO updateStrategy(Long id, PricingStrategyRequestDTO request) {
        PricingStrategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));

        // Atualizar campos
        strategy.setStrategyName(request.getName());
        strategy.setStrategyType(request.getStrategyType());
        strategy.setSpecificCategory(request.getSpecificCategory());
        strategy.setDaysBeforeEventStart(request.getDaysBeforeEventStart());
        strategy.setDaysBeforeEventEnd(request.getDaysBeforeEventEnd());
        strategy.setCustomStartDate(request.getCustomStartDate());
        strategy.setCustomEndDate(request.getCustomEndDate());
        strategy.setSalesThreshold(request.getSalesThreshold());
        strategy.setAvailableTicketsThreshold(request.getAvailableTicketsThreshold());
        strategy.setPercentageAdjustment(request.getPercentageAdjustment());
        strategy.setFixedAdjustment(request.getFixedAdjustment());
        strategy.setMultiplier(request.getMultiplier());
        strategy.setMinPrice(request.getMinPrice());
        strategy.setMaxPrice(request.getMaxPrice());
        strategy.setMinGroupSize(request.getMinGroupSize());
        strategy.setGroupDiscountPercentage(request.getGroupDiscountPercentage());
        strategy.setLoyaltyTier(request.getLoyaltyTier());
        strategy.setMinPurchases(request.getMinPurchases());
        strategy.setMinTotalSpent(request.getMinTotalSpent());
        strategy.setFirstTimeBuyerOnly(request.isFirstTimeBuyerOnly());
        strategy.setRepeatBuyerOnly(request.isRepeatBuyerOnly());
        strategy.setExclusiveToTier(request.isExclusiveToTier());
        strategy.setPriority(request.getPriority());
        strategy.setAutoApply(request.isAutoApply());

        PricingStrategy updated = strategyRepository.save(strategy);
        log.info("Updated pricing strategy: {}", updated.getStrategyName());

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteStrategy(Long id) {
        PricingStrategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));

        strategy.setState(LifeCycleState.DELETED); // Soft delete
        strategyRepository.save(strategy);
        log.info("Deactivated pricing strategy: {}", strategy.getStrategyName());
    }

    @Transactional(readOnly = true)
    public PricingStrategyResponseDTO getStrategy(Long id) {
        PricingStrategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));
        return convertToDTO(strategy);
    }

    @Transactional(readOnly = true)
    public List<PricingStrategyResponseDTO> getStrategiesByEvent(Long eventId) {


        List<PricingStrategyResponseDTO> list = strategyRepository.findByEventIdAndStateOrderByPriorityDesc(eventId, LifeCycleState.ACTIVE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return list;
    }

    // ==================== STRATEGY-SPECIFIC CREATION METHODS ====================

    @Transactional
    public PricingStrategyResponseDTO createTimeBasedStrategy(PricingStrategyRequestDTO request) {
        log.debug("⏰ Creating time-based strategy: {}", request.getName());

        // Validações específicas
        if (request.getDaysBeforeEventStart() == null && request.getCustomStartDate() == null) {
            throw new RuntimeException("Time-based strategy requires start date/time");
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            if (request.getDaysBeforeEventStart() != null) {
                request.setDescription(String.format(
                        "Preço especial %d dias antes do evento",
                        request.getDaysBeforeEventStart()
                ));
            } else {
                request.setDescription("Preço baseado no tempo até o evento");
            }
        }

        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createDemandBasedStrategy(PricingStrategyRequestDTO request) {
        log.debug("📊 Creating demand-based strategy: {}", request.getName());

        // Validações específicas
        if (request.getSalesThreshold() == null && request.getAvailableTicketsThreshold() == null) {
            throw new RuntimeException("Demand-based strategy requires threshold (sales or available tickets)");
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            if (request.getSalesThreshold() != null) {
                request.setDescription(String.format(
                        "Preço ajustado quando vendas atingirem %d%%",
                        request.getSalesThreshold()
                ));
            } else {
                request.setDescription("Preço baseado na demanda/procura");
            }
        }

        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createGroupDiscountStrategy(PricingStrategyRequestDTO request) {
        log.debug("👥 Creating group discount strategy: {}", request.getName());

        // Validações específicas
        if (request.getMinGroupSize() == null) {
            throw new RuntimeException("Group discount requires minimum group size");
        }

        if (request.getPercentageAdjustment() == null && request.getFixedAdjustment() == null) {
            throw new RuntimeException("Group discount requires price adjustment");
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            request.setDescription(String.format(
                    "Desconto para grupos de %d ou mais pessoas",
                    request.getMinGroupSize()
            ));
        }

        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createCategorySpecificStrategy(PricingStrategyRequestDTO request) {
        log.debug("🏷️ Creating category-specific strategy: {}", request.getName());

        // Validações específicas
        if (request.getSpecificCategory() == null) {
            throw new RuntimeException("Category-specific strategy requires a category");
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            request.setDescription(String.format(
                    "Estratégia específica para categoria: %s",
                    request.getSpecificCategory()
            ));
        }

        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createFlashSaleStrategy(PricingStrategyRequestDTO request) {
        log.debug("⚡ Creating flash sale strategy: {}", request.getName());

        // Validações específicas
        if (request.getCustomStartDate() == null || request.getCustomEndDate() == null) {
            throw new RuntimeException("Flash sale requires start and end dates");
        }

        if (request.getCustomEndDate().isBefore(request.getCustomStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            request.setDescription(String.format(
                    "Promoção relâmpago de %s até %s",
                    request.getCustomStartDate().toLocalDate(),
                    request.getCustomEndDate().toLocalDate()
            ));
        }

        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createLoyaltyStrategy(PricingStrategyRequestDTO request) {
        log.debug("💝 Creating loyalty strategy: {}", request.getName());

        // Validações específicas
        if (request.getLoyaltyTier() == null &&
                request.getMinPurchases() == null &&
                request.getMinTotalSpent() == null &&
                !request.isFirstTimeBuyerOnly() &&
                !request.isRepeatBuyerOnly()) {
            throw new RuntimeException("Loyalty strategy requires at least one condition");
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            request.setDescription(buildLoyaltyDescription(request));
        }

        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createTieredPricingStrategy(PricingStrategyRequestDTO request) {
        log.debug("📈 Creating tiered pricing strategy: {}", request.getName());

        // Validações específicas
        if (request.getMinPrice() == null || request.getMaxPrice() == null) {
            throw new RuntimeException("Tiered pricing requires min and max price");
        }

        if (request.getMinPrice().compareTo(request.getMaxPrice()) >= 0) {
            throw new RuntimeException("Min price must be less than max price");
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            request.setDescription(String.format(
                    "Preço varia entre %s e %s conforme demanda",
                    request.getMinPrice(),
                    request.getMaxPrice()
            ));
        }

        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createBundleStrategy(PricingStrategyRequestDTO request) {
        log.debug("📦 Creating bundle strategy: {}", request.getName());

        // Validações específicas
        if (request.getBundleTicketIds() == null || request.getBundleTicketIds().isEmpty()) {
            throw new RuntimeException("Bundle strategy requires at least one ticket");
        }

        if (request.getPercentageAdjustment() == null && request.getFixedAdjustment() == null) {
            throw new RuntimeException("Bundle strategy requires price adjustment");
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            request.setDescription("Desconto na compra de múltiplos ingressos");
        }

        // Nota: Lógica específica de bundle precisa ser implementada
        // Por enquanto, usa o método genérico
        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createFirstBuyerStrategy(PricingStrategyRequestDTO request) {
        log.debug("🎁 Creating first buyer strategy: {}", request.getName());

        // Configurações específicas
        request.setFirstTimeBuyerOnly(true);

        if (request.getPercentageAdjustment() == null && request.getFixedAdjustment() == null) {
            // Define desconto padrão de 15% se não especificado
            request.setPercentageAdjustment(new BigDecimal("-15"));
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            request.setDescription("Desconto especial para primeira compra");
        }

        return createStrategy(request);
    }

    @Transactional
    public PricingStrategyResponseDTO createVolumeBasedStrategy(PricingStrategyRequestDTO request) {
        log.debug("📊 Creating volume-based strategy: {}", request.getName());

        // Validações específicas
        if (request.getMinPurchases() == null) {
            throw new RuntimeException("Volume-based strategy requires minimum purchases");
        }

        if (request.getPercentageAdjustment() == null && request.getFixedAdjustment() == null) {
            // Define desconto padrão de 10% se não especificado
            request.setPercentageAdjustment(new BigDecimal("-10"));
        }

        // Configura descrição padrão se não fornecida
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            request.setDescription(String.format(
                    "Desconto para clientes com %d ou mais compras",
                    request.getMinPurchases()
            ));
        }

        return createStrategy(request);
    }

    // ==================== LOYALTY STRATEGIES (MULTIPLE CREATION) ====================

    @Transactional
    public List<PricingStrategyResponseDTO> createDefaultLoyaltyStrategies(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        List<PricingStrategy> strategies = new ArrayList<>();

        strategies.add(createLoyaltyStrategy(event, "PLATINUM", new BigDecimal("-20"), 1,
                "Desconto exclusivo para clientes PLATINUM"));
        strategies.add(createLoyaltyStrategy(event, "GOLD", new BigDecimal("-15"), 2,
                "Desconto especial para clientes GOLD"));
        strategies.add(createLoyaltyStrategy(event, "SILVER", new BigDecimal("-10"), 3,
                "Desconto para clientes SILVER"));
        strategies.add(createLoyaltyStrategy(event, "BRONZE", new BigDecimal("-5"), 4,
                "Desconto para clientes BRONZE"));
        strategies.add(createFirstBuyerStrategy(event, 5));
        strategies.add(createVolumeBasedStrategy(event, 6));
        strategies.add(createRepeatBuyerStrategy(event, 7));
        strategies.add(createHighSpenderStrategy(event, 8));

        List<PricingStrategy> saved = strategyRepository.saveAll(strategies);
        log.info("✅ Created {} loyalty strategies for event {}", saved.size(), eventId);

        return saved.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PricingStrategyResponseDTO> createCustomLoyaltyStrategies(
            Long eventId,
            boolean includePlatinum,
            boolean includeGold,
            boolean includeSilver,
            boolean includeBronze,
            boolean includeFirstBuyer,
            boolean includeVolumeBased,
            boolean includeRepeatBuyer,
            boolean includeHighSpender) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        List<PricingStrategy> strategies = new ArrayList<>();
        int priority = 1;

        if (includePlatinum) {
            strategies.add(createLoyaltyStrategy(event, "PLATINUM", new BigDecimal("-20"), priority++,
                    "Desconto exclusivo para clientes PLATINUM"));
        }
        if (includeGold) {
            strategies.add(createLoyaltyStrategy(event, "GOLD", new BigDecimal("-15"), priority++,
                    "Desconto especial para clientes GOLD"));
        }
        if (includeSilver) {
            strategies.add(createLoyaltyStrategy(event, "SILVER", new BigDecimal("-10"), priority++,
                    "Desconto para clientes SILVER"));
        }
        if (includeBronze) {
            strategies.add(createLoyaltyStrategy(event, "BRONZE", new BigDecimal("-5"), priority++,
                    "Desconto para clientes BRONZE"));
        }
        if (includeFirstBuyer) {
            strategies.add(createFirstBuyerStrategy(event, priority++));
        }
        if (includeVolumeBased) {
            strategies.add(createVolumeBasedStrategy(event, priority++));
        }
        if (includeRepeatBuyer) {
            strategies.add(createRepeatBuyerStrategy(event, priority++));
        }
        if (includeHighSpender) {
            strategies.add(createHighSpenderStrategy(event, priority++));
        }

        List<PricingStrategy> saved = strategyRepository.saveAll(strategies);
        log.info("✅ Created {} custom loyalty strategies for event {}", saved.size(), eventId);

        return saved.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private PricingStrategy createLoyaltyStrategy(Event event, String tier,
                                                  BigDecimal discount, int priority,
                                                  String description) {
        return PricingStrategy.builder()
                .strategyName(tier + " Member " + discount.abs() + "% OFF")
                .strategyType(PricingStrategyType.LOYALTY)
                .event(event)
                .loyaltyTier(tier)
                .percentageAdjustment(discount)
                .priority(priority)
                .autoApply(true)
                .timesApplied(0)
                .totalDiscountGiven(BigDecimal.ZERO)
                .totalRevenueGenerated(BigDecimal.ZERO)
                .description(description)
                .build();
    }

    private PricingStrategy createFirstBuyerStrategy(Event event, int priority) {
        return PricingStrategy.builder()
                .strategyName("🎁 First Timer 15% OFF")
                .strategyType(PricingStrategyType.FIRST_BUYER)
                .event(event)
                .firstTimeBuyerOnly(true)
                .percentageAdjustment(new BigDecimal("-15"))
                .priority(priority)
                .autoApply(true)
                .timesApplied(0)
                .totalDiscountGiven(BigDecimal.ZERO)
                .totalRevenueGenerated(BigDecimal.ZERO)
                .description("Desconto especial para primeira compra")
                .build();
    }

    private PricingStrategy createVolumeBasedStrategy(Event event, int priority) {
        return PricingStrategy.builder()
                .strategyName("⭐ Super Fã 25% OFF")
                .strategyType(PricingStrategyType.VOLUME_BASED)
                .event(event)
                .minPurchases(10)
                .percentageAdjustment(new BigDecimal("-25"))
                .priority(priority)
                .autoApply(true)
                .timesApplied(0)
                .totalDiscountGiven(BigDecimal.ZERO)
                .totalRevenueGenerated(BigDecimal.ZERO)
                .description("Desconto para clientes com mais de 10 compras")
                .build();
    }

    private PricingStrategy createRepeatBuyerStrategy(Event event, int priority) {
        return PricingStrategy.builder()
                .strategyName("🔄 Cliente Recorrente 10% OFF")
                .strategyType(PricingStrategyType.LOYALTY)
                .event(event)
                .repeatBuyerOnly(true)
                .percentageAdjustment(new BigDecimal("-10"))
                .priority(priority)
                .autoApply(true)
                .timesApplied(0)
                .totalDiscountGiven(BigDecimal.ZERO)
                .totalRevenueGenerated(BigDecimal.ZERO)
                .description("Desconto para clientes que já compraram antes")
                .build();
    }

    private PricingStrategy createHighSpenderStrategy(Event event, int priority) {
        return PricingStrategy.builder()
                .strategyName("💰 High Spender 20% OFF")
                .strategyType(PricingStrategyType.LOYALTY)
                .event(event)
                .minTotalSpent(new BigDecimal("5000"))
                .percentageAdjustment(new BigDecimal("-20"))
                .priority(priority)
                .autoApply(true)
                .timesApplied(0)
                .totalDiscountGiven(BigDecimal.ZERO)
                .totalRevenueGenerated(BigDecimal.ZERO)
                .description("Desconto para clientes que gastaram mais de R$5.000")
                .build();
    }

    private String buildLoyaltyDescription(PricingStrategyRequestDTO request) {
        List<String> conditions = new ArrayList<>();

        if (request.getLoyaltyTier() != null) {
            conditions.add("tier " + request.getLoyaltyTier());
        }
        if (request.getMinPurchases() != null) {
            conditions.add(request.getMinPurchases() + "+ compras");
        }
        if (request.getMinTotalSpent() != null) {
            conditions.add("gastou R$" + request.getMinTotalSpent() + "+");
        }
        if (request.isFirstTimeBuyerOnly()) {
            conditions.add("primeira compra");
        }
        if (request.isRepeatBuyerOnly()) {
            conditions.add("cliente recorrente");
        }
        if (request.isExclusiveToTier()) {
            conditions.add("exclusivo");
        }

        if (conditions.isEmpty()) {
            return "Benefício para clientes fiéis";
        }

        return "Desconto para: " + String.join(", ", conditions);
    }

    // ==================== APPLICATION OPERATIONS ====================

    @Transactional
    public StrategyApplicationResultDTO applyStrategyManually(Long strategyId) {
        PricingStrategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));

        if (!strategy.isActive()) {
            throw new RuntimeException("Strategy is not active");
        }

        return applyStrategyToEvent(strategy);
    }

    @Transactional
    public List<StrategyApplicationResultDTO> applyAutoStrategiesToEvent(Long eventId) {
        List<PricingStrategy> autoStrategies =
                strategyRepository.findByEventIdAndStateTrueAndAutoApplyTrueOrderByPriorityDesc(eventId);

        List<StrategyApplicationResultDTO> results = new ArrayList<>();

        for (PricingStrategy strategy : autoStrategies) {
            if (strategy.isCurrentlyApplicable()) {
                StrategyApplicationResultDTO result = applyStrategyToEvent(strategy);
                results.add(result);
            }
        }

        log.info("Applied {} auto-strategies to event: {}", results.size(), eventId);
        return results;
    }

    @Transactional
    public StrategyApplicationResultDTO applyStrategyToEvent(PricingStrategy strategy) {
        List<EventTicket> tickets = ticketRepository.findByEventId(strategy.getEvent().getId());

        List<TicketPriceChangeDTO> changes = new ArrayList<>();
        int affectedCount = 0;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (EventTicket ticket : tickets) {
            if (ticket.isAvailable() && strategy.appliesToTicket(ticket)) {
                TicketPriceChangeDTO change = applyStrategyToTicket(strategy, ticket);
                changes.add(change);

                if (change.isApplied()) {
                    affectedCount++;
                    if (change.getDifference() != null && change.getDifference().compareTo(BigDecimal.ZERO) < 0) {
                        totalDiscount = totalDiscount.add(change.getDifference().abs());
                    }
                }
            }
        }

        strategy.recordApplication(totalDiscount, null);
        strategyRepository.save(strategy);

        StrategyApplicationResultDTO result = StrategyApplicationResultDTO.builder()
                .strategyId(strategy.getId())
                .strategyName(strategy.getStrategyName())
                .appliedAt(LocalDateTime.now())
                .ticketsAffected(affectedCount)
                .priceChanges(changes)
                .totalDiscountApplied(totalDiscount)
                .status(affectedCount > 0 ? "SUCCESS" : "NO_TICKETS_AFFECTED")
                .message(affectedCount > 0 ?
                        "Applied to " + affectedCount + " tickets" :
                        "No tickets were affected by this strategy")
                .build();

        log.info("Applied strategy {} to {} tickets", strategy.getStrategyName(), affectedCount);
        return result;
    }

    private TicketPriceChangeDTO applyStrategyToTicket(PricingStrategy strategy, EventTicket ticket) {
        BigDecimal oldPrice = ticket.getCurrentPrice();
        BigDecimal newPrice = strategy.calculateAdjustedPrice(oldPrice);

        boolean applied = !newPrice.equals(oldPrice);

        if (applied) {
            ticket.updatePrice(newPrice,
                    "Estratégia aplicada: " + strategy.getStrategyName(),
                    "STRATEGY",
                    strategy.getId());

            ticketRepository.save(ticket);
        }

        return TicketPriceChangeDTO.builder()
                .ticketId(ticket.getId())
                .ticketName(ticket.getTicketName())
                .category(ticket.getCategory())
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .difference(newPrice.subtract(oldPrice))
                .applied(applied)
                .build();
    }

    // ==================== DEMAND-BASED CHECK ====================

    @Transactional
    public void checkAndApplyDemandBasedStrategies(Long eventId) {
        List<EventTicket> tickets = ticketRepository.findByEventId(eventId);

        for (EventTicket ticket : tickets) {
            int soldPercentage = (ticket.getSoldQuantity() * 100) / ticket.getTotalQuantity();

            List<PricingStrategy> demandStrategies =
                    strategyRepository.findStrategiesBySalesThreshold(eventId, soldPercentage);

            for (PricingStrategy strategy : demandStrategies) {
                if (strategy.isActive() && strategy.isAutoApply() && strategy.appliesToTicket(ticket)) {
                    applyStrategyToTicket(strategy, ticket);
                }
            }
        }
    }

    // ==================== CONVERSION METHODS ====================

    private PricingStrategyResponseDTO convertToDTO(PricingStrategy strategy) {
        long affectedTicketsCount = ticketRepository.countByEventIdAndCategory(
                strategy.getEvent().getId(),
                strategy.getSpecificCategory()
        );

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
                .availableTicketsThreshold(strategy.getAvailableTicketsThreshold())
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
                .exclusiveToTier(strategy.isExclusiveToTier())
                .description(strategy.getDescription())
                .priority(strategy.getPriority())
                .active(strategy.isActive())
                .autoApply(strategy.isAutoApply())
                .lastAppliedAt(strategy.getLastAppliedAt())
                .timesApplied(strategy.getTimesApplied())
                .totalDiscountGiven(strategy.getTotalDiscountGiven())
                .totalRevenueGenerated(strategy.getTotalRevenueGenerated())
                .createdAt(strategy.getCreatedAt())
                .updatedAt(strategy.getUpdatedAt())
                .createdBy(strategy.getCreatedBy())
                .currentlyApplicable(strategy.isCurrentlyApplicable())
                .affectedTicketsCount(affectedTicketsCount)
                .build();
    }

    /**
     * Cria uma estratégia de preço baseada no tipo fornecido no DTO
     * Este método analisa o strategyType e chama o método específico correspondente
     */
    @Transactional
    public PricingStrategyResponseDTO createStrategyByType(PricingStrategyRequestDTO request) {
        log.info("🎯 Creating strategy of type: {} with name: {}",
                request.getStrategyType(), request.getName());

        // Validações básicas
        if (request.getStrategyType() == null) {
            throw new RuntimeException("Strategy type is required");
        }

        if (request.getEventId() == null) {
            throw new RuntimeException("Event ID is required");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Strategy name is required");
        }

        // Roteamento baseado no tipo de estratégia
        switch (request.getStrategyType()) {
            // Estratégias baseadas em tempo
            case EARLY_BIRD:
            case LAST_MINUTE:
            case TIME_BASED:
                log.debug("⏰ Routing to time-based strategy");
                return createTimeBasedStrategy(request);

            // Estratégias baseadas em demanda
            case DEMAND_BASED:
                log.debug("📊 Routing to demand-based strategy");
                return createDemandBasedStrategy(request);

            // Descontos em grupo
            case GROUP_DISCOUNT:
                log.debug("👥 Routing to group discount strategy");
                return createGroupDiscountStrategy(request);

            // Estratégias por categoria
            case CATEGORY_SPECIFIC:
                log.debug("🏷️ Routing to category-specific strategy");
                return createCategorySpecificStrategy(request);

            // Promoções relâmpago
            case FLASH_SALE:
            case WEEKEND_SPECIAL:
                log.debug("⚡ Routing to flash sale strategy");
                return createFlashSaleStrategy(request);

            // Estratégias de fidelidade
            case LOYALTY_DISCOUNT:
            case LOYALTY:
                log.debug("💝 Routing to loyalty strategy");
                return createLoyaltyStrategy(request);

            // Preços por níveis
            case TIERED_PRICING:
                log.debug("📈 Routing to tiered pricing strategy");
                return createTieredPricingStrategy(request);

            // Descontos em pacote
            case BUNDLE_DISCOUNT:
            case BUNDLE:
                log.debug("📦 Routing to bundle strategy");
                return createBundleStrategy(request);

            // Primeira compra
            case FIRST_BUYER:
                log.debug("🎁 Routing to first buyer strategy");
                return createFirstBuyerStrategy(request);

            // Baseado em volume
            case VOLUME_BASED:
                log.debug("📊 Routing to volume-based strategy");
                return createVolumeBasedStrategy(request);

            // Se nenhum caso corresponder, usa o método genérico
            default:
                log.warn("⚠️ No specific handler for type: {}, using generic strategy",
                        request.getStrategyType());
                return createStrategy(request);
        }
    }
}