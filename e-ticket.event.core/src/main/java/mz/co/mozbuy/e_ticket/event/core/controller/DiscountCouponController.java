package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.CreateCouponDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.CouponResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.service.DiscountCouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class DiscountCouponController {

    private final DiscountCouponService discountCouponService;

    @PostMapping
    public ResponseEntity<CouponResponseDTO> createCoupon(@Valid @RequestBody CreateCouponDTO requestDTO) {
        CouponResponseDTO coupon = discountCouponService.createCoupon(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<CouponResponseDTO>> getEventCoupons(@PathVariable Long eventId) {
        List<CouponResponseDTO> coupons = discountCouponService.getCouponsByEventId(eventId);
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponseDTO> getCoupon(@PathVariable Long id) {
        CouponResponseDTO coupon = discountCouponService.getCouponById(id);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/code/{code}/event/{eventId}")
    public ResponseEntity<CouponResponseDTO> getCouponByCode(
            @PathVariable String code,
            @PathVariable Long eventId) {
        CouponResponseDTO coupon = discountCouponService.getCouponByCodeAndEvent(code, eventId);
        return ResponseEntity.ok(coupon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CreateCouponDTO requestDTO) {
        CouponResponseDTO coupon = discountCouponService.updateCoupon(id, requestDTO);
        return ResponseEntity.ok(coupon);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCoupon(@PathVariable Long id) {
        discountCouponService.deactivateCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateCoupon(@PathVariable Long id) {
        discountCouponService.activateCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        discountCouponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{eventId}/active")
    public ResponseEntity<List<CouponResponseDTO>> getActiveCoupons(@PathVariable Long eventId) {
        List<CouponResponseDTO> coupons = discountCouponService.getActiveCouponsByEventId(eventId);
        return ResponseEntity.ok(coupons);
    }
}