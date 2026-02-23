package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.CreateSaleDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.SaleResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.service.TicketSaleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class TicketSaleController {

    private final TicketSaleService ticketSaleService;

    @PostMapping
    public ResponseEntity<SaleResponseDTO> createSale(@Valid @RequestBody CreateSaleDTO saleDTO) {
        SaleResponseDTO sale = ticketSaleService.createSale(saleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(sale);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<SaleResponseDTO> getSale(@PathVariable String transactionId) {
        SaleResponseDTO sale = ticketSaleService.getSaleByTransactionId(transactionId);
        return ResponseEntity.ok(sale);
    }

    @PostMapping("/{transactionId}/confirm-payment")
    public ResponseEntity<SaleResponseDTO> confirmPayment(
            @PathVariable String transactionId,
            @RequestParam String paymentMethod,
            @RequestParam String paymentReference) {
        SaleResponseDTO sale = ticketSaleService.processPayment(transactionId, paymentMethod, paymentReference);
        return ResponseEntity.ok(sale);
    }

    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<SaleResponseDTO> cancelSale(
            @PathVariable String transactionId,
            @RequestParam(required = false) String reason) {
        SaleResponseDTO sale = ticketSaleService.cancelSale(transactionId, reason != null ? reason : "User cancelled");
        return ResponseEntity.ok(sale);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<SaleResponseDTO>> getEventSales(@PathVariable Long eventId) {
        List<SaleResponseDTO> sales = ticketSaleService.getSalesByEventId(eventId);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<SaleResponseDTO>> getOrganizerSales(@PathVariable Long organizerId) {
        List<SaleResponseDTO> sales = ticketSaleService.getSalesByOrganizerId(organizerId);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/check-coupon")
    public ResponseEntity<Double> checkCoupon(
            @RequestParam Long ticketId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String couponCode) {
        Double finalPrice = ticketSaleService.calculatePriceWithCoupon(ticketId, quantity, couponCode);
        return ResponseEntity.ok(finalPrice);
    }
}