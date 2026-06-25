//package mz.co.mozbuy.e_ticket.event.core.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import mz.co.mozbuy.common.audit.AuditableEntity;
//import mz.co.mozbuy.common.audit.LifeCycleState;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "ticket_reservation")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class TicketReservationEntity extends AuditableEntity<Long, String> {
//
//    @Column(name = "payment_transaction_id", nullable = false, length = 50)
//    private String paymentTransactionId;
//
//    @Column(name = "sale_id", nullable = false)
//    private Long saleId;
//
//    @Column(name = "event_id", nullable = false)
//    private Long eventId;
//
//    @Column(name = "ticket_id", nullable = false)
//    private Long ticketId;
//
//    @Column(name = "user_id", nullable = false)
//    private Long userId;
//
//    @Column(nullable = false)
//    private Integer quantity;
//
//    @Column(name = "expires_at", nullable = false)
//    private LocalDateTime expiresAt;
//
//    @Column(name = "confirmed_at")
//    private LocalDateTime confirmedAt;
//
//    @Column(name = "confirmed_by", length = 100)
//    private String confirmedBy;
//
//    @Column(name = "cancelled_at")
//    private LocalDateTime cancelledAt;
//
//    @Column(name = "cancelled_by", length = 100)
//    private String cancelledBy;
//
//    @Column(name = "cancel_reason", length = 255)
//    private String cancelReason;
//
//    @PrePersist
//    protected void onCreate() {
//        if (expiresAt == null) {
//            // Para métodos síncronos: 30 min
//            // Para métodos assíncronos: pode ser sobrescrito
//            expiresAt = LocalDateTime.now().plusMinutes(30);
//        }
//    }
//
//    public boolean isActive() { return getState() == LifeCycleState.ACTIVE; }
//    public boolean isConfirmed() { return getState() == LifeCycleState.CONFIRMED; }
//    public boolean isExpired() { return getState() == LifeCycleState.ACTIVE && LocalDateTime.now().isAfter(expiresAt); }
//    public boolean isCancelled() { return getState() == LifeCycleState.CANCELLED; }
//
//    public void confirm(String confirmedBy) {
//        setState(LifeCycleState.CONFIRMED);
//        this.confirmedAt = LocalDateTime.now();
//        this.confirmedBy = confirmedBy;
//    }
//
//    public void cancel(String reason, String cancelledBy) {
//        setState(LifeCycleState.CANCELLED);
//        this.cancelledAt = LocalDateTime.now();
//        this.cancelledBy = cancelledBy;
//        this.cancelReason = reason;
//    }
//
//    public void expire() {
//        setState(LifeCycleState.EXPIRED);
//        this.cancelledAt = LocalDateTime.now();
//        this.cancelReason = "Reserva expirada por timeout";
//    }
//}