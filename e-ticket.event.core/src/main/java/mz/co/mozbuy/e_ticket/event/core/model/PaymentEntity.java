package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;


import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um pagamento realizado por um usuário.
 * Pode estar associado a diferentes tipos de entidades (evento, reserva, etc.).
 *
 * author jmenete
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity extends AuditableEntity<Long, String> {

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_payment_user"))
    private String user_name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency = "MZN";

    @Column(length = 50)
    private String method;

    @Column(name = "provider_payment_id", length = 150)
    private String providerPaymentId;

    @Column(length = 20)
    private String status = "PENDING";

    /**
     * Tipo de entidade relacionada (ex: "Reservation", "Ticket", etc.)
     */
    @Column(name = "related_type", length = 30)
    private String relatedType;

    /**
     * ID da entidade relacionada
     */
    @Column(name = "related_id")
    private Long relatedId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "life_cycle_state", nullable = false)
    private LifeCycleState lifeCycleState = LifeCycleState.ACTIVE;

    // Hooks de auditoria
    @PrePersist
    protected void onCreate() {
        super.setCreatedAt(LocalDateTime.now());
        if (this.lifeCycleState == null) {
            this.lifeCycleState = LifeCycleState.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        super.setUpdatedAt(LocalDateTime.now());
    }
}
