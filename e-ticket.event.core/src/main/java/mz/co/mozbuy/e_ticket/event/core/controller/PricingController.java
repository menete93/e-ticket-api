//package mz.co.mozbuy.e_ticket.event.core.controller;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import mz.co.mozbuy.e_ticket.event.core.dto.*;
//import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
//import mz.co.mozbuy.e_ticket.event.core.service.PricingService;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/pricing")
//@RequiredArgsConstructor
//public class PricingController {
//
//    private final PricingService pricingService;
//
//    // ==================== ESTRATÉGIAS ====================
//
//    @PostMapping("/strategies")
//    public ResponseEntity<PricingStrategyResponseDTO> createPricingStrategy(
//            @Valid @RequestBody PricingStrategyRequestDTO requestDTO) {
//        PricingStrategyResponseDTO strategy = pricingService.createPricingStrategy(requestDTO);
//        return ResponseEntity.status(HttpStatus.CREATED).body(strategy);
//    }
//
//    @PutMapping("/strategies/{id}")
//    public ResponseEntity<PricingStrategyResponseDTO> updatePricingStrategy(
//            @PathVariable Long id,
//            @Valid @RequestBody PricingStrategyRequestDTO requestDTO) {
//        PricingStrategyResponseDTO strategy = pricingService.updatePricingStrategy(id, requestDTO);
//        return ResponseEntity.ok(strategy);
//    }
//
//    @DeleteMapping("/strategies/{id}")
//    public ResponseEntity<Void> deletePricingStrategy(@PathVariable Long id) {
//        pricingService.deletePricingStrategy(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/strategies/{id}")
//    public ResponseEntity<PricingStrategyResponseDTO> getPricingStrategy(@PathVariable Long id) {
//        PricingStrategyResponseDTO strategy = pricingService.getPricingStrategy(id);
//        return ResponseEntity.ok(strategy);
//    }
//
//    @GetMapping("/strategies")
//    public ResponseEntity<List<PricingStrategyResponseDTO>> findAllStrategies() {
//        List<PricingStrategyResponseDTO> strategies = pricingService.findAllStrategies();
//        return ResponseEntity.ok(strategies);
//    }
//
//    @GetMapping("/events/{eventId}/strategies")
//    public ResponseEntity<List<PricingStrategyResponseDTO>> getStrategiesByEvent(
//            @PathVariable Long eventId) {
//        List<PricingStrategyResponseDTO> strategies = pricingService.getStrategiesByEvent(eventId);
//        return ResponseEntity.ok(strategies);
//    }
//
//    @GetMapping("/strategies/types")
//    public ResponseEntity<List<Map<String, String>>> getStrategyTypes() {
//        List<Map<String, String>> strategies = Arrays.stream(PricingStrategyType.values())
//                .map(strategy -> Map.of(
//                        "key", strategy.name(),
//                        "displayName", strategy.getDisplayName(),
//                        "description", strategy.getDescription()
//                ))
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(strategies);
//    }
//
//    // ==================== APLICAÇÃO DE ESTRATÉGIAS ====================
//
//    @PostMapping("/strategies/{strategyId}/apply")
//    public ResponseEntity<StrategyApplicationResultDTO> applyPricingStrategy(
//            @PathVariable Long strategyId) {
//        StrategyApplicationResultDTO result = pricingService.applyStrategyManually(strategyId);
//        return ResponseEntity.ok(result);
//    }
//
//    @PostMapping("/strategies/{strategyId}/apply-to-ticket/{ticketId}")
//    public ResponseEntity<BigDecimal> applyStrategyToTicket(
//            @PathVariable Long strategyId,
//            @PathVariable Long ticketId,
//            @RequestParam(required = false) Long userId) {
//        BigDecimal newPrice = pricingService.applyStrategyToTicket(strategyId, ticketId, userId);
//        return ResponseEntity.ok(newPrice);
//    }
//
//    @PostMapping("/events/{eventId}/apply-auto-strategies")
//    public ResponseEntity<List<StrategyApplicationResultDTO>> applyAutoStrategiesToEvent(
//            @PathVariable Long eventId) {
//        List<StrategyApplicationResultDTO> results = pricingService.applyAutoStrategiesToEvent(eventId);
//        return ResponseEntity.ok(results);
//    }
//
//    @PostMapping("/events/{eventId}/apply-dynamic-pricing")
//    public ResponseEntity<String> applyDynamicPricingToEvent(@PathVariable Long eventId) {
//        // Este método pode chamar o scheduler manualmente
//        pricingService.applyAutoStrategiesToEvent(eventId);
//        return ResponseEntity.ok("Dynamic pricing applied to event");
//    }
//
//    // ==================== ESTRATÉGIAS DE FIDELIDADE ====================
//
//    @PostMapping("/events/{eventId}/create-loyalty-strategies")
//    public ResponseEntity<List<PricingStrategyResponseDTO>> createLoyaltyStrategies(
//            @PathVariable Long eventId) {
//        List<PricingStrategyResponseDTO> strategies =
//                pricingService.getPricingStrategyService().createDefaultLoyaltyStrategies(eventId);
//        return ResponseEntity.status(HttpStatus.CREATED).body(strategies);
//    }
//
//    @PostMapping("/events/{eventId}/create-custom-loyalty-strategies")
//    public ResponseEntity<List<PricingStrategyResponseDTO>> createCustomLoyaltyStrategies(
//            @PathVariable Long eventId,
//            @RequestBody LoyaltyStrategiesRequestDTO request) {
//        List<PricingStrategyResponseDTO> strategies =
//                pricingService.getPricingStrategyService().createCustomLoyaltyStrategies(
//                        eventId,
//                        request.isIncludePlatinum(),
//                        request.isIncludeGold(),
//                        request.isIncludeSilver(),
//                        request.isIncludeBronze(),
//                        request.isIncludeFirstBuyer(),
//                        request.isIncludeVolumeBased(),
//                        request.isIncludeRepeatBuyer(),
//                        request.isIncludeHighSpender());
//        return ResponseEntity.status(HttpStatus.CREATED).body(strategies);
//    }
//
//    // ==================== MUDANÇAS PROGRAMADAS ====================
//
//    @PostMapping("/scheduled-changes")
//    public ResponseEntity<ScheduledPriceChangeResponseDTO> schedulePriceChange(
//            @Valid @RequestBody ScheduledPriceChangeRequestDTO requestDTO) {
//        ScheduledPriceChangeResponseDTO scheduledChange = pricingService.schedulePriceChange(requestDTO);
//        return ResponseEntity.status(HttpStatus.CREATED).body(scheduledChange);
//    }
//
//    @GetMapping("/events/{eventId}/scheduled-changes")
//    public ResponseEntity<List<ScheduledPriceChangeResponseDTO>> getScheduledChangesByEvent(
//            @PathVariable Long eventId) {
//        List<ScheduledPriceChangeResponseDTO> changes = pricingService.getScheduledChangesByEvent(eventId);
//        return ResponseEntity.ok(changes);
//    }
//
//    @DeleteMapping("/scheduled-changes/{changeId}")
//    public ResponseEntity<Void> cancelScheduledChange(@PathVariable Long changeId) {
//        pricingService.cancelScheduledChange(changeId);
//        return ResponseEntity.noContent().build();
//    }
//
//    // ==================== HISTÓRICO DE PREÇOS ====================
//
//    @GetMapping("/history/ticket/{ticketId}")
//    public ResponseEntity<List<TicketPriceHistoryDTO>> getPriceHistoryByTicket(
//            @PathVariable Long ticketId) {
//        List<TicketPriceHistoryDTO> history = pricingService.getPriceHistoryByTicket(ticketId);
//        return ResponseEntity.ok(history);
//    }
//
//    @GetMapping("/history/events/{eventId}")
//    public ResponseEntity<List<TicketPriceHistoryDTO>> getPriceHistoryByEvent(
//            @PathVariable Long eventId) {
//        List<TicketPriceHistoryDTO> history = pricingService.getPriceHistoryByEvent(eventId);
//        return ResponseEntity.ok(history);
//    }
//
//    @GetMapping("/history/strategy/{strategyId}")
//    public ResponseEntity<List<TicketPriceHistoryDTO>> getPriceHistoryByStrategy(
//            @PathVariable Long strategyId) {
//        List<TicketPriceHistoryDTO> history = pricingService.getPriceHistoryByStrategy(strategyId);
//        return ResponseEntity.ok(history);
//    }
//
//    @GetMapping("/history/ticket/{ticketId}/latest")
//    public ResponseEntity<TicketPriceHistoryDTO> getLatestPriceHistory(
//            @PathVariable Long ticketId) {
//        return pricingService.getPriceHistoryByTicket(ticketId)
//                .stream()
//                .findFirst()
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // ==================== PREVISÕES E RELATÓRIOS ====================
//
//    @GetMapping("/predict/revenue/{eventId}")
//    public ResponseEntity<RevenuePredictionDTO> predictRevenue(@PathVariable Long eventId) {
//        RevenuePredictionDTO prediction = pricingService.predictRevenue(eventId);
//        return ResponseEntity.ok(prediction);
//    }
//
//    @GetMapping("/predict/price/{ticketId}")
//    public ResponseEntity<BigDecimal> predictFuturePrice(
//            @PathVariable Long ticketId,
//            @RequestParam(required = false) Long userId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime futureDate) {
//        BigDecimal predictedPrice = pricingService.predictFuturePrice(ticketId, userId, futureDate);
//        return ResponseEntity.ok(predictedPrice);
//    }
//
//    @GetMapping("/reports/effectiveness/{eventId}")
//    public ResponseEntity<List<PricingStrategyEffectivenessDTO>> getStrategyEffectivenessReport(
//            @PathVariable Long eventId) {
//        List<PricingStrategyEffectivenessDTO> report = pricingService.getStrategyEffectivenessReport(eventId);
//        return ResponseEntity.ok(report);
//    }
//}