package mz.co.mozbuy.common.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "softDeleteFilter", parameters = @ParamDef(name = "deletedState", type = Integer.class))
@Filter(name = "softDeleteFilter", condition = "life_cycle_state <> :deletedState")
public abstract class AuditableEntity<ID, U> extends DomainEntity<ID> {

    // ==================== Auditoria ====================
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false, length = 100)
    private U createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private U updatedBy;

    // ==================== Ciclo de vida ====================
//    @Convert(converter = LifeCycleStateConverter.class)
    @Column(name = "life_cycle_state", nullable = false)
    private LifeCycleState lifeCycleState = LifeCycleState.ACTIVE;

    // ==================== Métodos de ciclo de vida ====================
    public void activate() { this.lifeCycleState = LifeCycleState.ACTIVE; }
    public void inactivate() { this.lifeCycleState = LifeCycleState.INACTIVE; }
    public void delete() { this.lifeCycleState = LifeCycleState.DELETED; }
    public void block() { this.lifeCycleState = LifeCycleState.BLOCKED; }
    public void ban() { this.lifeCycleState = LifeCycleState.BANNED; }

    public boolean isActive() { return lifeCycleState == LifeCycleState.ACTIVE; }
    public boolean isInactive() { return lifeCycleState == LifeCycleState.INACTIVE; }
    public boolean isDeleted() { return lifeCycleState == LifeCycleState.DELETED; }
    public boolean isBlocked() { return lifeCycleState == LifeCycleState.BLOCKED; }
    public boolean isBanned() { return lifeCycleState == LifeCycleState.BANNED; }

    // ==================== Soft delete automático ====================
    @PreRemove
    public void softDelete() { this.lifeCycleState = LifeCycleState.DELETED; }
}
