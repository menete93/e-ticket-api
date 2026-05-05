package mz.co.mozbuy.common.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.converter.LifeCycleStateConverter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity<ID, U> extends DomainEntity<ID> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2027641277643165127L;

    // ==================== Auditoria ====================
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false, length = 100)
    private U createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private U updatedBy;

    // ==================== Ciclo de vida ====================

    @Convert(converter = LifeCycleStateConverter.class)
    @Column(name = "state", nullable = false, length = 50)
    private LifeCycleState state = LifeCycleState.ACTIVE;

    // ==================== Métodos de ciclo de vida ====================
    public void activate() { this.state = LifeCycleState.ACTIVE; }
    public void inactivate() { this.state = LifeCycleState.INACTIVE; }
    public void delete() { this.state = LifeCycleState.DELETED; }
    public void block() { this.state = LifeCycleState.BLOCKED; }
    public void ban() { this.state = LifeCycleState.BANNED; }

    public boolean isActive() { return state == LifeCycleState.ACTIVE; }
    public boolean isInactive() { return state == LifeCycleState.INACTIVE; }
    public boolean isDeleted() { return state == LifeCycleState.DELETED; }
    public boolean isBlocked() { return state == LifeCycleState.BLOCKED; }
    public boolean isBanned() { return state == LifeCycleState.BANNED; }

    // ==================== Soft delete automático ====================
    @PreRemove
    public void softDelete() { this.state = LifeCycleState.DELETED; }
}
