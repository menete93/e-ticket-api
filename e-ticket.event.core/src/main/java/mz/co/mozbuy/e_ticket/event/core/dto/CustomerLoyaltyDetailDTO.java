package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerLoyaltyDetailDTO {
    private Long userId;
    private String tier;
    private Integer totalPurchases;
    private BigDecimal totalSpent;
    private BigDecimal averageTicketValue;
    private String favoriteCategory;
    private LocalDateTime firstPurchaseDate;
    private LocalDateTime lastPurchaseDate;
    private BigDecimal currentDiscount;
    private BigDecimal nextTierDiscount;
    private BigDecimal purchasesToNextTier;
    private BigDecimal spentToNextTier;
    private String message;
}