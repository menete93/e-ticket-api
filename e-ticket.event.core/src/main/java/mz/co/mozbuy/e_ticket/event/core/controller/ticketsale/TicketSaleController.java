package mz.co.mozbuy.e_ticket.event.core.controller.ticketsale;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.*;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceCalculationRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceCalculationResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.service.TicketSaleService; // 👈 Importar TicketSaleService
import mz.co.mozbuy.e_ticket.event.core.service.calculate.TicketPricingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketSaleController {

    private final TicketPricingService pricingService;
    private final TicketSaleService ticketSaleService; // 👈 Usar TicketSaleService em vez de CheckoutService

    /**
     * Endpoint para calcular preço antes de finalizar
     * Os dados do usuário vêm no payload (front-end envia a partir do token)
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<PriceCalculationResponseDTO> calculatePrice(
            @Valid @RequestBody PriceCalculationRequestDTO request) {

        log.info("📊 Calculando preço para {} tickets do evento: {}",
                request.getTicketQuantities().size(),
                request.getEventId());

        PriceCalculationResponseDTO response = pricingService.calculatePrice(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para finalizar compra (checkout)
     * Usa CreateSaleDTO que contém os dados do usuário
     */
    @PostMapping("/checkout")
    public ResponseEntity<SaleResponseDTO> checkout(
            @Valid @RequestBody CreateSaleDTO request) {

        log.info("💳 Processando checkout para ticket: {}, quantity: {}, user: {}",
                request.getTicketId(),
                request.getQuantity(),
                request.getUserId());

        SaleResponseDTO response = ticketSaleService.createSale(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para buscar venda por transactionId
     */
    @GetMapping("/sale/{transactionId}")
    public ResponseEntity<SaleResponseDTO> getSaleByTransactionId(
            @PathVariable String transactionId) {

        log.info("🔍 Buscando venda com transactionId: {}", transactionId);

        SaleResponseDTO response = ticketSaleService.getSaleByTransactionId(transactionId);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para buscar vendas por evento
     */
    @GetMapping("/event/{eventId}/sales")
    public ResponseEntity<List<SaleResponseDTO>> getSalesByEventId(
            @PathVariable("eventId")  Long eventId) {

        log.info("🔍 Buscando vendas do evento: {}", eventId);

        List<SaleResponseDTO> response = ticketSaleService.getSalesByEventId(eventId);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para buscar vendas por usuário
     */
    @GetMapping("/user/{userId}/sales")
    public ResponseEntity<List<SaleResponseDTO>> getSalesByUserId(
            @PathVariable Long userId) {

        log.info("🔍 Buscando vendas do usuário: {}", userId);

        // Implementar se necessário
        // List<SaleResponseDTO> response = ticketSaleService.getSalesByUserId(userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para calcular preço com cupom (simplificado)
     */
    @GetMapping("/calculate-with-coupon")
    public ResponseEntity<Double> calculateWithCoupon(
            @RequestParam Long ticketId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String couponCode) {

        log.info("🧮 Calculando preço com cupom: ticketId={}, quantity={}, coupon={}",
                ticketId, quantity, couponCode);

        Double price = ticketSaleService.calculatePriceWithCoupon(ticketId, quantity, couponCode);

        return ResponseEntity.ok(price);
    }

    /**
     * Endpoint para processar pagamento (após confirmação do gateway)
     */
    @PostMapping("/sale/{transactionId}/process-payment")
    public ResponseEntity<SaleResponseDTO> processPayment(
            @PathVariable String transactionId,
            @RequestParam String paymentMethod,
            @RequestParam String paymentReference) {

        log.info("💰 Processando pagamento para transação: {}", transactionId);

        SaleResponseDTO response = ticketSaleService.processPayment(
                transactionId, paymentMethod, paymentReference);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para cancelar venda
     */
    @PostMapping("/sale/{transactionId}/cancel")
    public ResponseEntity<SaleResponseDTO> cancelSale(
            @PathVariable String transactionId,
            @RequestParam String reason) {

        log.info("❌ Cancelando venda: {}, motivo: {}", transactionId, reason);

        SaleResponseDTO response = ticketSaleService.cancelSale(transactionId, reason);

        return ResponseEntity.ok(response);
    }
}