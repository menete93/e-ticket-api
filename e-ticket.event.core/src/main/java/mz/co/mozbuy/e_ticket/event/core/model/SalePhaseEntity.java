package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa uma fase de venda (Sale Phase) de um evento.
 * Cada fase pode ter um preço multiplicador e um intervalo de tempo definido.
 *
 * Exemplo: "Pré-venda", "Venda normal", "Última hora".
 *
 * author jmenete
 */
@Entity
@Table(name = "sale_phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePhaseEntity extends AuditableEntity<Long, String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sale_phase_event"))
    private EventEntity event;

    @Column(length = 50)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * Multiplicador aplicado ao preço base dos bilhetes nesta fase.
     * Exemplo: 1.0 = preço normal, 0.9 = 10% desconto, 1.2 = 20% acréscimo.
     */
    @Column(name = "price_multiplier", precision = 5, scale = 2, nullable = false)
    private BigDecimal priceMultiplier = BigDecimal.valueOf(1.0);

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
