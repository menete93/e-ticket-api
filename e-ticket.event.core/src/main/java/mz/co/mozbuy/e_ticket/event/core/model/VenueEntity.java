package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueEntity extends AuditableEntity<Long, String> {

    // Dono do espaço
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    /**
     * Campo geográfico compatível com PostGIS (GEOGRAPHY(POINT, 4326))
     * Necessita de Hibernate Spatial
     */
    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;

    @Column(length = 255)
    private String address;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    // Ciclo de vida
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "life_cycle_state", nullable = false)
    private LifeCycleState lifeCycleState = LifeCycleState.ACTIVE;

    // Hooks automáticos
    @PrePersist
    protected void onCreate() {
        this.setCreatedAt(LocalDateTime.now());
        if (this.lifeCycleState == null) {
            this.lifeCycleState = LifeCycleState.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.setUpdatedAt(LocalDateTime.now());
    }
}
