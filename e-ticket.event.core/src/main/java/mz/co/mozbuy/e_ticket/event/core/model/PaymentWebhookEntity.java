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
@Table(name = "PAYMENT_WEBHOOK", indexes = {
        @Index(name = "IDX_PAYMENT_WEBHOOK_01", columnList = "WEBHOOK_ID"),
        @Index(name = "IDX_PAYMENT_WEBHOOK_02", columnList = "PROVIDER_CODE"),
        @Index(name = "IDX_PAYMENT_WEBHOOK_03", columnList = "STATUS")
})
@SequenceGenerator(name = "GENERATOR", sequenceName = "PAYMENT_WEBHOOK_SEQ", initialValue = 1, allocationSize = 1)
public class PaymentWebhookEntity extends AuditableEntity<Long, String> {

    @Column(name = "WEBHOOK_ID", unique = true, length = 50)
    private String webhookId;

    @Column(name = "PROVIDER_CODE", length = 30)
    private String providerCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "PAYLOAD", columnDefinition = "json")
    private Map<String, Object> payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "HEADERS", columnDefinition = "json")
    private Map<String, String> headers;

    @Column(name = "SIGNATURE", length = 255)
    private String signature;

    @Column(name = "STATUS", length = 20)
    private String status;  // RECEIVED, PROCESSED, FAILED

    @Column(name = "PROCESSED_AT")
    private LocalDateTime processedAt;

    @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;
}