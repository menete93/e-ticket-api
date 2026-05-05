package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.enums.DiscountType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_coupons")
@Getter
@Setter
public class DiscountCoupon extends AuditableEntity<Long, String> {

    @Column(nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType = DiscountType.PERCENTAGE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "valid_from")
    private LocalDateTime validFrom = LocalDateTime.now();

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(length = 200)
    private String description;

    @Column(name = "min_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    // Métodos de negócio
    public boolean isValid() {

        if(this.getState() != LifeCycleState.ACTIVE){
            return false;

        }

        if (maxUses != null && usedCount >= maxUses) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        return validUntil == null || !now.isAfter(validUntil);
    }

    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        if (!isValid() || discountValue == null) {
            return originalPrice.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discountedPrice;

        switch (discountType) {
            case PERCENTAGE -> {
                final BigDecimal discountPercentage = discountValue
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                final BigDecimal discountAmount = originalPrice.multiply(discountPercentage);
                discountedPrice = originalPrice.subtract(discountAmount);
            }
            case FIXED_AMOUNT -> discountedPrice = originalPrice.subtract(discountValue);
            default -> discountedPrice = originalPrice;
        }

        // Garantir que o preço não seja negativo e arredondar para 2 casas decimais
        return discountedPrice.max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }


    public void useCoupon() {
        if (usedCount == null) {
            usedCount = 0;
        }
        usedCount++;
    }

    public boolean canApplyToPurchase(BigDecimal purchaseAmount) {
        if (minPurchaseAmount != null && purchaseAmount.compareTo(minPurchaseAmount) < 0) {
            return false;
        }
        return isValid();
    }

}