package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um bilhete adquirido por um usuário para um evento específico.
 * Contém informações sobre preço, categoria, assento e QR code de validação.
 *
 * author jmenete
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketEntity extends AuditableEntity<Long, String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_event"))
    private EventEntity event;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_ticket_user"))
    private String user_name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_ticket_category"))
    private TicketCategoryEntity category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "seat_number", length = 30)
    private String seatNumber;

    @Column(name = "qr_code", length = 255)
    private String qrCode;

    @Column(name = "qr_signature", length = 255)
    private String qrSignature;

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
