package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.*;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.factory.StrategyFactory;
import mz.co.mozbuy.e_ticket.event.core.service.BulkStrategyService;
import mz.co.mozbuy.e_ticket.event.core.service.PricingStrategyService;

import mz.co.mozbuy.e_ticket.event.core.service.LoyaltyService;
import mz.co.mozbuy.e_ticket.event.core.service.pricing.PricingHistoryService;
import mz.co.mozbuy.e_ticket.event.core.service.pricing.PricingStrategyRouterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/pricing/strategies")
@RequiredArgsConstructor
public class PricingStrategyController {

    private final PricingStrategyService strategyService;
    private final PricingHistoryService historyService;
    private final LoyaltyService loyaltyService; // Se precisar
    private final PricingStrategyRouterService strategyRouterService; // NOVO
    private final StrategyFactory strategyFactory;
    private final BulkStrategyService bulkStrategyService;


    // ==================== CRUD ENDPOINTS ====================


    /**
     * Endpoint que recebe o payload mínimo do front-end
     */
    @PostMapping("/bulk-create")
    public ResponseEntity<List<PricingStrategyResponseDTO>> createStrategiesBulk(
            @Valid @RequestBody BulkStrategyAssignmentDTO request) {

        log.info("📥 Recebida requisição para criar estratégias para o evento: {}",
                request.getEventId());

        // Usa o serviço que integra helpers e validações
        List<PricingStrategyResponseDTO> responses =
                bulkStrategyService.createStrategiesFromAssignment(request);

        log.info("✅ {} estratégias criadas com sucesso", responses.size());

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }


    /**
     * Endpoint unificado para criar qualquer tipo de estratégia
     * O tipo é determinado pelo campo strategyType no DTO
     */
    @PostMapping("/create")
    public ResponseEntity<PricingStrategyResponseDTO> createStrategy(
            @Valid @RequestBody PricingStrategyRequestDTO request) {

        log.info("📥 Recebida requisição para criar estratégia do tipo: {}",
                request.getStrategyType());

        // Delega para o router service que decide qual método chamar
        PricingStrategyResponseDTO response = strategyRouterService.createStrategyByType(request);

        log.info("✅ Estratégia criada com sucesso. ID: {}, Tipo: {}",
                response.getId(), response.getStrategyType());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint unificado para atualizar qualquer tipo de estratégia
     */
    @PutMapping("/{id}/update")
    public ResponseEntity<PricingStrategyResponseDTO> updateStrategy(
            @PathVariable Long id,
            @Valid @RequestBody PricingStrategyRequestDTO request) {

        log.info("📝 Atualizando estratégia ID: {} do tipo: {}", id, request.getStrategyType());

        PricingStrategyResponseDTO response = strategyRouterService.updateStrategyByType(id, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStrategy(@PathVariable Long id) {
        strategyService.deleteStrategy(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingStrategyResponseDTO> getStrategy(@PathVariable Long id) {
        PricingStrategyResponseDTO response = strategyService.getStrategy(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<PricingStrategyResponseDTO>> getStrategiesByEvent(
            @PathVariable Long eventId) {
        List<PricingStrategyResponseDTO> strategies = strategyService.getStrategiesByEvent(eventId);
        return ResponseEntity.ok(strategies);
    }

    // ==================== LOYALTY STRATEGIES ENDPOINT ====================

    /**
     * Endpoint para criar estratégias de fidelidade padrão para um evento
     * Exemplo: POST /api/pricing/strategies/event/1/create-loyalty-strategies
     */
    @PostMapping("/event/{eventId}/create-loyalty-strategies")
    public ResponseEntity<List<PricingStrategyResponseDTO>> createLoyaltyStrategies(
            @PathVariable Long eventId) {
        List<PricingStrategyResponseDTO> strategies =
                strategyService.createDefaultLoyaltyStrategies(eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(strategies);
    }

    /**
     * Versão alternativa com parâmetros personalizáveis
     */
    @PostMapping("/event/{eventId}/create-custom-loyalty-strategies")
    public ResponseEntity<List<PricingStrategyResponseDTO>> createCustomLoyaltyStrategies(
            @PathVariable Long eventId,
            @RequestParam(required = false, defaultValue = "false") boolean includePlatinum,
            @RequestParam(required = false, defaultValue = "false") boolean includeGold,
            @RequestParam(required = false, defaultValue = "false") boolean includeSilver,
            @RequestParam(required = false, defaultValue = "false") boolean includeBronze,
            @RequestParam(required = false, defaultValue = "false") boolean includeFirstBuyer,
            @RequestParam(required = false, defaultValue = "false") boolean includeVolumeBased,
            @RequestParam(required = false, defaultValue = "false") boolean includeRepeatBuyer,
            @RequestParam(required = false, defaultValue = "false") boolean includeHighSpender) {

        List<PricingStrategyResponseDTO> strategies =
                strategyService.createCustomLoyaltyStrategies(
                        eventId,
                        includePlatinum,
                        includeGold,
                        includeSilver,
                        includeBronze,
                        includeFirstBuyer,
                        includeVolumeBased,
                        includeRepeatBuyer,
                        includeHighSpender);

        return ResponseEntity.status(HttpStatus.CREATED).body(strategies);
    }
    // ==================== APPLICATION ENDPOINTS ====================

    @PostMapping("/{id}/apply")
    public ResponseEntity<StrategyApplicationResultDTO> applyStrategyManually(
            @PathVariable Long id) {
        StrategyApplicationResultDTO result = strategyService.applyStrategyManually(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/event/{eventId}/apply-auto")
    public ResponseEntity<List<StrategyApplicationResultDTO>> applyAutoStrategies(
            @PathVariable Long eventId) {
        List<StrategyApplicationResultDTO> results =
                strategyService.applyAutoStrategiesToEvent(eventId);
        return ResponseEntity.ok(results);
    }

    // ==================== HISTORY ENDPOINTS ====================

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TicketPriceHistoryDTO>> getStrategyHistory(
            @PathVariable Long id) {
        List<TicketPriceHistoryDTO> history = historyService.getHistoryByStrategy(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/tickets-affected")
    public ResponseEntity<List<TicketPriceChangeDTO>> getTicketsAffected(
            @PathVariable Long id) {
        List<TicketPriceChangeDTO> affected = historyService.getTicketsAffectedByStrategy(id);
        return ResponseEntity.ok(affected);
    }

    // ==================== ENUMS ENDPOINT ====================

    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getStrategyTypes() {
        List<Map<String, String>> strategies = Arrays.stream(PricingStrategyType.values())
                .map(strategy -> Map.of(
                        "key", strategy.name(),
                        "displayName", strategy.getDisplayName(),
                        "description", strategy.getDescription()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(strategies);
    }

}