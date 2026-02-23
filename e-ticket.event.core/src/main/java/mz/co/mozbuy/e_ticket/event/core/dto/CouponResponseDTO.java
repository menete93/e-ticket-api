package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.enums.DiscountType;
import mz.co.mozbuy.e_ticket.event.core.model.DiscountCoupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponResponseDTO {

    private Long id;
    private String code;
    private Long eventId;
    private String eventName;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Integer maxUses;
    private Integer usedCount;
    private Integer remainingUses;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isActive;
    private String description;
    private BigDecimal minPurchaseAmount;
    private Boolean isPublic;
    private Boolean isValid;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}