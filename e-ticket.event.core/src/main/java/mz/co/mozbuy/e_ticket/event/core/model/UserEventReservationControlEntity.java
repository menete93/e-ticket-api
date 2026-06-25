package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_event_reservation_control")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventReservationControlEntity extends AuditableEntity<Long, String> {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Builder.Default
    @Column(name = "total_attempts")
    private Integer totalAttempts = 0;

    @Builder.Default
    @Column(name = "active_reservations")
    private Integer activeReservations = 0;

    @Builder.Default
    @Column(name = "completed_purchases")
    private Integer completedPurchases = 0;

    @Builder.Default
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Builder.Default
    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    @Column(name = "block_reason", length = 255)
    private String blockReason;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "first_attempt_at")
    private LocalDateTime firstAttemptAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    public void registerAttempt() {
        this.totalAttempts++;
        this.lastAttemptAt = LocalDateTime.now();
        if (this.firstAttemptAt == null) this.firstAttemptAt = LocalDateTime.now();
    }

    public void registerActiveReservation() { this.activeReservations++; registerAttempt(); }
    public void releaseActiveReservation() { this.activeReservations = Math.max(0, this.activeReservations - 1); }
    public void registerCompletedPurchase() { this.completedPurchases++; this.activeReservations = Math.max(0, this.activeReservations - 1); }
    public void registerFailedAttempt() { this.failedAttempts++; this.activeReservations = Math.max(0, this.activeReservations - 1); }

    public void block(String reason) {
        this.isBlocked = true;
        this.blockReason = reason;
        this.blockedAt = LocalDateTime.now();
        setState(LifeCycleState.BLOCKED);
    }

    public void unblock() {
        this.isBlocked = false;
        this.blockReason = null;
        this.blockedAt = null;
        setState(LifeCycleState.ACTIVE);
    }

//    public boolean canReserve() {
//        return !this.isBlocked && this.activeReservations < 1 && this.completedPurchases == 0 && this.isActive();
//    }

    public boolean canReserve(int quantity) {
        return !this.isBlocked
                && (this.activeReservations + this.completedPurchases + quantity) <= 50
                && this.isActive();
    }
}