package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "payment_pending")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPendingEntity extends AuditableEntity<Long, String> {

    @Column(name = "payment_transaction_id", nullable = false, unique = true, length = 50)
    private String paymentTransactionId;

    @Column(name = "pending_type", nullable = false, length = 30)
    private String pendingType;

    @Column(name = "payment_url", length = 500)
    private String paymentUrl;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(length = 20)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", columnDefinition = "jsonb")
    private Map<String, Object> requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private Map<String, Object> responsePayload;

    @PrePersist
    protected void onCreate() {
        if (expiresAt == null) expiresAt = LocalDateTime.now().plusMinutes(60);
        if (status == null) status = "ACTIVE";
    }

    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    public void expire() { this.status = "EXPIRED"; }
}