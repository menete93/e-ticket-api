package mz.co.mozbuy.e_ticket.event.core.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.ScheduledPriceChangeRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.ScheduledPriceChangeResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.service.PricingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/strategies")
    public ResponseEntity<PricingStrategyResponseDTO> createPricingStrategy(
            @Valid @RequestBody PricingStrategyRequestDTO requestDTO) {
        PricingStrategyResponseDTO strategy = pricingService.createPricingStrategy(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(strategy);
    }

    @PostMapping("/scheduled-changes")
    public ResponseEntity<ScheduledPriceChangeResponseDTO> schedulePriceChange(
            @Valid @RequestBody ScheduledPriceChangeRequestDTO requestDTO) {
        ScheduledPriceChangeResponseDTO scheduledChange = pricingService.schedulePriceChange(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduledChange);
    }

    @PostMapping("/strategies/{strategyId}/apply")
    public ResponseEntity<String> applyPricingStrategy(@PathVariable Long strategyId) {
        pricingService.applyPricingStrategy(strategyId);
        return ResponseEntity.ok("Pricing strategy applied successfully");
    }

    @PostMapping("/events/{eventId}/apply-dynamic-pricing")
    public ResponseEntity<String> applyDynamicPricingToEvent(@PathVariable Long eventId) {
        pricingService.applyDynamicPricingToEvent(eventId);
        return ResponseEntity.ok("Dynamic pricing applied to event");
    }

    @GetMapping
    public List<String> getAll() {
        return Arrays.stream(PricingStrategyType.values())
                .map(PricingStrategyType::getValue)
                .toList();
    }
}
