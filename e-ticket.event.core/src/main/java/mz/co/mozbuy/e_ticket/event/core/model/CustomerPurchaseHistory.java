package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import mz.co.mozbuy.common.audit.AuditableEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_purchase_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPurchaseHistory extends AuditableEntity<Long, String> {

    @Column(name = "user_id", nullable = false)
    private Long userId; // ID do cliente

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event; // Último evento comprado

    @Column(name = "total_purchases", nullable = false)
    private Integer totalPurchases = 0; // Número total de compras

    @Column(name = "total_tickets_bought", nullable = false)
    private Integer totalTicketsBought = 0; // Total de ingressos comprados

    @Column(name = "total_spent", precision = 15, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO; // Valor total gasto

    @Column(name = "last_purchase_date")
    private LocalDateTime lastPurchaseDate; // Data da última compra

    @Column(name = "first_purchase_date")
    private LocalDateTime firstPurchaseDate; // Data da primeira compra

    @Column(name = "favorite_category")
    private String favoriteCategory; // Categoria preferida

    @Column(name = "purchase_frequency")
    private Double purchaseFrequency; // Frequência média (compras/mês)

    @Column(name = "average_ticket_value", precision = 15, scale = 2)
    private BigDecimal averageTicketValue; // Valor médio por compra

    @Column(name = "loyalty_tier")
    private String loyaltyTier; // BRONZE, SILVER, GOLD, PLATINUM

    // Métodos de utilidade
    public void recordPurchase(Integer quantity, BigDecimal amount, LocalDateTime date, Event event) {
        this.totalPurchases++;
        this.totalTicketsBought += quantity;
        this.totalSpent = this.totalSpent.add(amount);
        this.lastPurchaseDate = date;

        if (this.firstPurchaseDate == null) {
            this.firstPurchaseDate = date;
        }

        // Calcular valor médio
        this.averageTicketValue = this.totalSpent
                .divide(BigDecimal.valueOf(totalPurchases), 2, java.math.RoundingMode.HALF_UP);

        // Calcular frequência
        if (this.firstPurchaseDate != null && this.lastPurchaseDate != null) {
            long daysBetween = java.time.Duration.between(firstPurchaseDate, lastPurchaseDate).toDays();
            if (daysBetween > 0) {
                this.purchaseFrequency = (totalPurchases * 30.0) / daysBetween; // compras por mês
            }
        }

        // Atualizar tier baseado em gasto total
        updateLoyaltyTier();
    }

    private void updateLoyaltyTier() {
        if (totalSpent.compareTo(new BigDecimal("5000")) >= 0) {
            this.loyaltyTier = "PLATINUM";
        } else if (totalSpent.compareTo(new BigDecimal("2000")) >= 0) {
            this.loyaltyTier = "GOLD";
        } else if (totalSpent.compareTo(new BigDecimal("500")) >= 0) {
            this.loyaltyTier = "SILVER";
        } else if (totalPurchases >= 3) {
            this.loyaltyTier = "BRONZE";
        } else {
            this.loyaltyTier = "NEW";
        }
    }

    public boolean isFirstTimeBuyer() {
        return totalPurchases == 0;
    }

    public boolean isLoyalCustomer() {
        return totalPurchases >= 5 || totalSpent.compareTo(new BigDecimal("1000")) >= 0;
    }
}