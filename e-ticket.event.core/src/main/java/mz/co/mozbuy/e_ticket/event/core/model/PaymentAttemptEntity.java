package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "PAYMENT_ATTEMPT", indexes = {
        @Index(name = "IDX_PAYMENT_ATTEMPT_01", columnList = "TRANSACTION_ID"),
        @Index(name = "IDX_PAYMENT_ATTEMPT_02", columnList = "CREATED_AT")
})
@SequenceGenerator(name = "GENERATOR", sequenceName = "PAYMENT_ATTEMPT_SEQ", initialValue = 1, allocationSize = 1)
public class PaymentAttemptEntity extends AuditableEntity<Long, String> {

    @ManyToOne
    @JoinColumn(name = "TRANSACTION_ID", referencedColumnName = "TRANSACTION_ID")
    private PaymentTransactionEntity transaction;

    @Column(name = "ATTEMPT_NUMBER", nullable = false)
    private Integer attemptNumber;

    @Column(name = "STATUS", length = 30)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "REQUEST_PAYLOAD", columnDefinition = "json")
    private Map<String, Object> requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "RESPONSE_PAYLOAD", columnDefinition = "json")
    private Map<String, Object> responsePayload;

    @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "DURATION_MS")
    private Long durationMs;

    @Column(name = "PROCESSED_AT")
    private LocalDateTime processedAt;
}