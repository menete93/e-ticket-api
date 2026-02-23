package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.CreateCouponDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.CouponResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.exceptions.EventNotFoundException;
import mz.co.mozbuy.e_ticket.event.core.model.DiscountCoupon;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.repository.DiscountCouponRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountCouponService {

    private final DiscountCouponRepository discountCouponRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CouponResponseDTO createCoupon(CreateCouponDTO requestDTO) {
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new EventNotFoundException(requestDTO.getEventId()));

        // Verificar se código já existe para este evento
        if (discountCouponRepository.existsByCodeAndEventId(requestDTO.getCode(), event.getId())) {
            throw new RuntimeException("Coupon with code " + requestDTO.getCode() + " already exists for this event");
        }

        DiscountCoupon coupon = requestDTO.toEntity();
        coupon.setEvent(event);

        // Definir datas padrão se não fornecidas
        if (coupon.getValidFrom() == null) {
            coupon.setValidFrom(LocalDateTime.now());
        }

        DiscountCoupon savedCoupon = discountCouponRepository.save(coupon);

        // Adicionar ao evento
        event.addDiscountCoupon(savedCoupon);
        eventRepository.save(event);

        log.info("Created coupon: {} for event: {}", coupon.getCode(), event.getId());

        return toResponseDTO(savedCoupon);
    }

    public List<CouponResponseDTO> getCouponsByEventId(Long eventId) {
        List<DiscountCoupon> coupons = discountCouponRepository.findByEventId(eventId);
        return coupons.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CouponResponseDTO getCouponById(Long id) {
        DiscountCoupon coupon = discountCouponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
        return toResponseDTO(coupon);
    }

    public CouponResponseDTO getCouponByCodeAndEvent(String code, Long eventId) {
        DiscountCoupon coupon = discountCouponRepository.findByCodeAndEventId(code, eventId)
                .orElseThrow(() -> new RuntimeException("Coupon not found with code: " + code + " for event: " + eventId));
        return toResponseDTO(coupon);
    }

    @Transactional
    public CouponResponseDTO updateCoupon(Long id, CreateCouponDTO requestDTO) {
        DiscountCoupon coupon = discountCouponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        // Verificar se está tentando mudar para um código que já existe
        if (!coupon.getCode().equals(requestDTO.getCode()) &&
                discountCouponRepository.existsByCodeAndEventId(requestDTO.getCode(), coupon.getEvent().getId())) {
            throw new RuntimeException("Coupon with code " + requestDTO.getCode() + " already exists for this event");
        }

        // Atualizar campos
        coupon.setCode(requestDTO.getCode());
        coupon.setDiscountType(requestDTO.getDiscountType());
        coupon.setDiscountValue(requestDTO.getDiscountValue());
        coupon.setMaxUses(requestDTO.getMaxUses());
        coupon.setValidFrom(requestDTO.getValidFrom());
        coupon.setValidUntil(requestDTO.getValidUntil());
        coupon.setDescription(requestDTO.getDescription());
        coupon.setMinPurchaseAmount(requestDTO.getMinPurchaseAmount());
        coupon.setIsPublic(requestDTO.getIsPublic());

        DiscountCoupon updatedCoupon = discountCouponRepository.save(coupon);
        log.info("Updated coupon: {}", coupon.getCode());

        return toResponseDTO(updatedCoupon);
    }

    @Transactional
    public void deactivateCoupon(Long id) {
        DiscountCoupon coupon = discountCouponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        coupon.setIsActive(false);
        discountCouponRepository.save(coupon);
        log.info("Deactivated coupon: {}", coupon.getCode());
    }

    @Transactional
    public void activateCoupon(Long id) {
        DiscountCoupon coupon = discountCouponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        coupon.setIsActive(true);
        discountCouponRepository.save(coupon);
        log.info("Activated coupon: {}", coupon.getCode());
    }

    @Transactional
    public void deleteCoupon(Long id) {
        DiscountCoupon coupon = discountCouponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        discountCouponRepository.delete(coupon);
        log.info("Deleted coupon: {}", coupon.getCode());
    }

    public List<CouponResponseDTO> getActiveCouponsByEventId(Long eventId) {
        List<DiscountCoupon> coupons = discountCouponRepository.findActiveCouponsForEvent(eventId, LocalDateTime.now());
        return coupons.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public boolean validateCoupon(String code, Long eventId, Double purchaseAmount) {
        try {
            DiscountCoupon coupon = discountCouponRepository.findByCodeAndEventId(code, eventId)
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));

            return coupon.isValid() && coupon.canApplyToPurchase(purchaseAmount != null ?
                    java.math.BigDecimal.valueOf(purchaseAmount) : null);
        } catch (Exception e) {
            log.warn("Coupon validation failed: {}", e.getMessage());
            return false;
        }
    }

    private CouponResponseDTO toResponseDTO(DiscountCoupon coupon) {
        CouponResponseDTO dto = new CouponResponseDTO();
        dto.setId(coupon.getId());
        dto.setCode(coupon.getCode());
        dto.setEventId(coupon.getEvent().getId());
        dto.setEventName(coupon.getEvent().getName());
        dto.setDiscountType(coupon.getDiscountType());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setMaxUses(coupon.getMaxUses());
        dto.setUsedCount(coupon.getUsedCount());
        dto.setRemainingUses(coupon.getMaxUses() != null ?
                coupon.getMaxUses() - coupon.getUsedCount() : null);
        dto.setValidFrom(coupon.getValidFrom());
        dto.setValidUntil(coupon.getValidUntil());
        dto.setIsActive(coupon.getIsActive());
        dto.setDescription(coupon.getDescription());
        dto.setMinPurchaseAmount(coupon.getMinPurchaseAmount());
        dto.setIsPublic(coupon.getIsPublic());
        dto.setIsValid(coupon.isValid());
        dto.setCreatedAt(coupon.getCreatedAt());
        dto.setUpdatedAt(coupon.getUpdatedAt());
        return dto;
    }
}