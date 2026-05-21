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
@Table(name = "PAYMENT_REFUND", indexes = {
        @Index(name = "IDX_PAYMENT_REFUND_01", columnList = "TRANSACTION_ID"),
        @Index(name = "IDX_PAYMENT_REFUND_02", columnList = "REFUND_TRANSACTION_ID")
})
@SequenceGenerator(name = "GENERATOR", sequenceName = "PAYMENT_REFUND_SEQ", initialValue = 1, allocationSize = 1)
public class PaymentRefundEntity extends AuditableEntity<Long, String> {

    @ManyToOne
    @JoinColumn(name = "TRANSACTION_ID", referencedColumnName = "TRANSACTION_ID")
    private PaymentTransactionEntity transaction;

    @Column(name = "REFUND_TRANSACTION_ID", unique = true, length = 50)
    private String refundTransactionId;

    @Column(name = "AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "REASON", length = 255)
    private String reason;

    @Column(name = "STATUS", length = 30)
    private String status;  // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "REQUESTED_BY", length = 100)
    private String requestedBy;

    @Column(name = "APPROVED_BY", length = 100)
    private String approvedBy;

    @Column(name = "APPROVED_AT")
    private LocalDateTime approvedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @Column(name = "EXTERNAL_REFUND_ID", length = 100)
    private String externalRefundId;
}
