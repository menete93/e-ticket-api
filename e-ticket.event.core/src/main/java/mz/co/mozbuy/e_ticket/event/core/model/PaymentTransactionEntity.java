package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionEntity extends AuditableEntity<Long, String> {

    @Column(name = "reservation_code", nullable = false, unique = true, length = 50)
    private String reservationCode;

    @Column(name = "sale_id", nullable = false)
    private Long saleId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    @Column(name = "payment_method_code", length = 30)
    private String paymentMethodCode;

    @Column(name = "status", nullable = false, length = 30)
    private String status;  // PENDING, SUCCESS, FAILED, EXPIRED, CANCELLED

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        if (currency == null) currency = "MZN";
        if (status == null) status = "PENDING";
        if (expiresAt == null) expiresAt = LocalDateTime.now().plusMinutes(30);
    }

    // Métodos helpers
    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isSuccess() { return "SUCCESS".equals(status); }
    public boolean isFailed() { return "FAILED".equals(status); }
    public boolean isExpired() { return "EXPIRED".equals(status); }

    public void markSuccess() {
        this.status = "SUCCESS";
        this.confirmedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public void markExpired() {
        this.status = "EXPIRED";
    }
}