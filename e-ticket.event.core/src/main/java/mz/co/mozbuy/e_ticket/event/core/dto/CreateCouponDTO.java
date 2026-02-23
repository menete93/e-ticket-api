package mz.co.mozbuy.e_ticket.event.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.enums.DiscountType;
import mz.co.mozbuy.e_ticket.event.core.model.DiscountCoupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateCouponDTO {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    private Integer maxUses;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;

    private String description;

    private BigDecimal minPurchaseAmount;

    private Boolean isPublic = true;

    public DiscountCoupon toEntity() {
        DiscountCoupon coupon = new DiscountCoupon();
        coupon.setCode(this.code);
        coupon.setDiscountType(this.discountType);
        coupon.setDiscountValue(this.discountValue);
        coupon.setMaxUses(this.maxUses);
        coupon.setValidFrom(this.validFrom);
        coupon.setValidUntil(this.validUntil);
        coupon.setDescription(this.description);
        coupon.setMinPurchaseAmount(this.minPurchaseAmount);
        coupon.setIsPublic(this.isPublic);
        coupon.setIsActive(true);
        return coupon;
    }
}