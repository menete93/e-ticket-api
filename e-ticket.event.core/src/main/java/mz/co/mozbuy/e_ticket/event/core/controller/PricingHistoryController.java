package mz.co.mozbuy.e_ticket.event.core.controller;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketPriceHistoryDTO;
import mz.co.mozbuy.e_ticket.event.core.service.pricing.PricingHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/pricing/history")
@RequiredArgsConstructor
public class PricingHistoryController {

    private final PricingHistoryService pricingHistoryService;

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<TicketPriceHistoryDTO>> getHistoryByTicket(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(pricingHistoryService.getHistoryByTicket(ticketId));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketPriceHistoryDTO>> getHistoryByEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(pricingHistoryService.getHistoryByEvent(eventId));
    }

    @GetMapping("/ticket/{ticketId}/date-range")
    public ResponseEntity<List<TicketPriceHistoryDTO>> getHistoryByDateRange(
            @PathVariable Long ticketId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(
                pricingHistoryService.getHistoryByTicketAndDateRange(ticketId, start, end)
        );
    }

    @GetMapping("/ticket/{ticketId}/latest")
    public ResponseEntity<TicketPriceHistoryDTO> getLatestHistory(
            @PathVariable Long ticketId) {
        return pricingHistoryService.getLatestHistoryByTicket(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ticket/{ticketId}/count")
    public ResponseEntity<Long> getPriceChangeCount(@PathVariable Long ticketId) {
        return ResponseEntity.ok(pricingHistoryService.getPriceChangeCount(ticketId));
    }
}