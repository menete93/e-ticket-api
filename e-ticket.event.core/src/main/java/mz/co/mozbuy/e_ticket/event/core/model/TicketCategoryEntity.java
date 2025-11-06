package mz.co.mozbuy.e_ticket.event.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.util.JsonAttributeConverter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Representa uma categoria de bilhete associada a um evento.
 * Contém informações sobre preço base, quantidade e condições especiais de convite.
 *
 * @author jmenete
 */
@Entity
@Table(name = "ticket_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategoryEntity extends AuditableEntity<Long, String> {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_category_event"))
    @JsonIgnore
    private EventEntity event;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "allocated_quantity", nullable = false)
    private Integer allocatedQuantity = 0;

    /**
     * Campo JSONB — condições de convite, mapeadas como Map<String, Object>.
     * Exemplo: {"type": "VIP", "requiresApproval": true}
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> inviteCondition;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "life_cycle_state", nullable = false)
    private LifeCycleState lifeCycleState = LifeCycleState.ACTIVE;

    // Hooks automáticos
    @PrePersist
    protected void onCreate() {
        super.setCreatedAt(java.time.LocalDateTime.now());
        if (this.lifeCycleState == null) {
            this.lifeCycleState = LifeCycleState.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        super.setUpdatedAt(java.time.LocalDateTime.now());
    }
}
