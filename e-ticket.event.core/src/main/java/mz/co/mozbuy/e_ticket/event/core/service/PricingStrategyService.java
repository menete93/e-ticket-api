package mz.co.mozbuy.e_ticket.event.core.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.PricingStrategyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingStrategyService {

    private final PricingStrategyRepository pricingStrategyRepository;
    private final EventRepository eventRepository;

    @Transactional
    public PricingStrategyResponseDTO createPricingStrategy(PricingStrategyRequestDTO requestDTO, String createdBy) {
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + requestDTO.getEventId()));

        // Verificar se já existe estratégia com mesmo nome
        if (pricingStrategyRepository.existsByEventIdAndStrategyNameAndIsActiveTrue(
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
        strategy.setDemandThresholdPercentage(requestDTO.getDemandThresholdPercentage());
        strategy.setPriceIncreasePercentage(requestDTO.getPriceIncreasePercentage());
        strategy.setIsActive(requestDTO.getIsActive());
        strategy.setApplyAutomatically(requestDTO.getApplyAutomatically());
        strategy.setDescription(requestDTO.getDescription());
        strategy.setCreatedBy(createdBy);

        PricingStrategy savedStrategy = pricingStrategyRepository.save(strategy);
        log.info("Created pricing strategy: {} for event: {}", savedStrategy.getStrategyName(), event.getId());

        return toDTO(savedStrategy);
    }

    public PricingStrategyResponseDTO getPricingStrategyById(Long id) {
        PricingStrategy strategy = pricingStrategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing strategy not found with id: " + id));
        return toDTO(strategy);
    }

    @Transactional
    public PricingStrategyResponseDTO updatePricingStrategy(Long id, PricingStrategyRequestDTO requestDTO) {
        PricingStrategy strategy = pricingStrategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing strategy not found with id: " + id));

        strategy.setStrategyName(requestDTO.getStrategyName());
        strategy.setStrategyType(requestDTO.getStrategyType());
        strategy.setBasePrice(requestDTO.getBasePrice());
        strategy.setMinPrice(requestDTO.getMinPrice());
        strategy.setMaxPrice(requestDTO.getMaxPrice());
        strategy.setDemandMultiplier(requestDTO.getDemandMultiplier());
        strategy.setTimeBasedIncreaseDays(requestDTO.getTimeBasedIncreaseDays());
        strategy.setTimeBasedIncreasePercentage(requestDTO.getTimeBasedIncreasePercentage());
        strategy.setGroupSizeThreshold(requestDTO.getGroupSizeThreshold());
        strategy.setGroupDiscountPercentage(requestDTO.getGroupDiscountPercentage());
        strategy.setDemandThresholdPercentage(requestDTO.getDemandThresholdPercentage());
        strategy.setPriceIncreasePercentage(requestDTO.getPriceIncreasePercentage());
        strategy.setIsActive(requestDTO.getIsActive());
        strategy.setApplyAutomatically(requestDTO.getApplyAutomatically());
        strategy.setDescription(requestDTO.getDescription());

        PricingStrategy updatedStrategy = pricingStrategyRepository.save(strategy);
        log.info("Updated pricing strategy: {}", updatedStrategy.getStrategyName());

        return toDTO(updatedStrategy);
    }

    @Transactional
    public void deletePricingStrategy(Long id) {
        PricingStrategy strategy = pricingStrategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing strategy not found with id: " + id));

        strategy.setIsActive(false);
        pricingStrategyRepository.save(strategy);

        log.info("Deactivated pricing strategy: {}", strategy.getStrategyName());
    }

    @Transactional
    public void applyPricingStrategy(Long strategyId) {
        PricingStrategy strategy = pricingStrategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Pricing strategy not found with id: " + strategyId));

        if (!strategy.getIsActive()) {
            throw new RuntimeException("Pricing strategy is not active");
        }

        // Aqui você implementaria a lógica para aplicar a estratégia aos bilhetes
        // Por enquanto, apenas atualizamos a data de última aplicação
        strategy.setLastAppliedAt(LocalDateTime.now());
        pricingStrategyRepository.save(strategy);

        log.info("Applied pricing strategy: {} to event: {}", strategy.getStrategyName(), strategy.getEvent().getId());
    }

    private PricingStrategyResponseDTO toDTO(PricingStrategy strategy) {
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
        dto.setDemandThresholdPercentage(strategy.getDemandThresholdPercentage());
        dto.setPriceIncreasePercentage(strategy.getPriceIncreasePercentage());
        dto.setIsActive(strategy.getIsActive());
        dto.setApplyAutomatically(strategy.getApplyAutomatically());
        dto.setLastAppliedAt(strategy.getLastAppliedAt());
        dto.setDescription(strategy.getDescription());
        dto.setCreatedAt(strategy.getCreatedAt());
        dto.setUpdatedAt(strategy.getUpdatedAt());
        dto.setCreatedBy(strategy.getCreatedBy());
        dto.setUpdatedBy(strategy.getUpdatedBy());
        return dto;
    }
}
