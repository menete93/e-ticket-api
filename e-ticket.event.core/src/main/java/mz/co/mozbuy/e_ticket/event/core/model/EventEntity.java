package mz.co.mozbuy.e_ticket.event.core.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;
import org.locationtech.jts.geom.Point;
import org.n52.jackson.datatype.jts.GeometryDeserializer;
import org.n52.jackson.datatype.jts.GeometrySerializer;

import java.time.LocalDateTime;

/**
 * Representa um evento que ocorre num local (Venue) e pertence a uma categoria.
 *
 * @author jmenete
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity extends AuditableEntity<Long, String> {

    /**
     * voltar para criar a juncao com o user logado
     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private String organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", referencedColumnName = "id")
    private VenueEntity venue;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private EventCategoryEntity category;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column
    private Integer capacity;

    @Column(name = "seat_type", length = 20)
    private String seatType = "FREE";

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    /**
     * Campo geográfico compatível com PostGIS (GEOGRAPHY(POINT, 4326)).
     * Necessita da dependência hibernate-spatial.
     */
    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    @JsonSerialize(using = GeometrySerializer.class)
    @JsonDeserialize(contentUsing = GeometryDeserializer.class)
    private Point location;

    @Column(name = "status", length = 20)
    private String status = "DRAFT";

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "life_cycle_state", nullable = false)
    private LifeCycleState lifeCycleState = LifeCycleState.ACTIVE;

    // Callbacks automáticos
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
