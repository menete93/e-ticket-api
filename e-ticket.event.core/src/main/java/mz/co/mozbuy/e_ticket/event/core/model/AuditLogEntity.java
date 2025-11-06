package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.e_ticket.event.core.util.JsonAttributeConverter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Representa o log de auditoria de ações realizadas sobre entidades do sistema.
 * Permite armazenar detalhes em formato JSON.
 *
 * author jmenete
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntity extends AuditableEntity<Long, String> {

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 50)
    private String action;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "performed_by", foreignKey = @ForeignKey(name = "fk_auditlog_user"))
    private String performedBy;

    /**
     * Detalhes da ação em formato JSON, mapeado como Map<String, Object>
     */
    @Convert(converter = JsonAttributeConverter.class)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
