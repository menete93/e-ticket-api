package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment_refund", schema = "e_ticket", indexes = {
        @Index(name = "IDX_PAYMENT_REFUND_01", columnList = "payment_transaction_id"),
        @Index(name = "IDX_PAYMENT_REFUND_02", columnList = "refund_transaction_id")
})
public class PaymentRefundEntity extends AuditableEntity<Long, String> {

    // ✅ Relacionamento ManyToOne com PaymentTransactionEntity (referencia o ID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id", referencedColumnName = "id", nullable = false)
    private PaymentTransactionEntity transaction;

    @Column(name = "refund_transaction_id", unique = true, length = 50)
    private String refundTransactionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String reason;

    @Column(length = 30)
    private String status;  // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "external_refund_id", length = 100)
    private String externalRefundId;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = "PENDING";
    }
}