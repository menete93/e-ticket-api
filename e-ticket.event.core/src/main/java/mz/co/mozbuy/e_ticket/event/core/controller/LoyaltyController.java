package mz.co.mozbuy.e_ticket.event.core.controller;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.LoyaltyReportDTO;
import mz.co.mozbuy.e_ticket.event.core.model.CustomerPurchaseHistory;
import mz.co.mozbuy.e_ticket.event.core.service.LoyaltyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/customer/{userId}")
    public ResponseEntity<CustomerPurchaseHistory> getCustomerHistory(@PathVariable Long userId) {
        return loyaltyService.getCustomerHistory(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/report")
    public ResponseEntity<LoyaltyReportDTO> getLoyaltyReport() {
        return ResponseEntity.ok(loyaltyService.generateLoyaltyReport());
    }

    @GetMapping("/top-spenders")
    public ResponseEntity<List<CustomerPurchaseHistory>> getTopSpenders() {
        return ResponseEntity.ok(loyaltyService.getTopSpenders());
    }

    @GetMapping("/tier/{tier}/count")
    public ResponseEntity<Long> getCustomersByTier(@PathVariable String tier) {
        return ResponseEntity.ok(loyaltyService.countByTier(tier));
    }
}