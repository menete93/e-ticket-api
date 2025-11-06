package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;

import java.time.LocalDateTime;

/**
 * Representa uma reserva de um usuário para um espaço (Venue).
 * Contém informações sobre horário, número de pessoas, status e pagamento.
 *
 * author jmenete
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEntity extends AuditableEntity<Long, String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_venue"))
    private VenueEntity venue;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_reservation_user"))
    private String user_name;

    @Column(name = "reservation_start", nullable = false)
    private LocalDateTime reservationStart;

    @Column(name = "reservation_end")
    private LocalDateTime reservationEnd;

    @Column(name = "num_people")
    private Integer numPeople;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "qr_code", length = 255)
    private String qrCode;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "UNPAID";

    // Se quiseres adicionar ciclo de vida:
    // @Enumerated(EnumType.ORDINAL)
    // @Column(name = "life_cycle_state", nullable = false)
    // private LifeCycleState lifeCycleState = LifeCycleState.ACTIVE;

    @PrePersist
    protected void onCreate() {
        if (super.getCreatedAt() == null) {
            super.setCreatedAt(LocalDateTime.now());
        }
    }
}
