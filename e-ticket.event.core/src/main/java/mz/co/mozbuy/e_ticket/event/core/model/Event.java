package mz.co.mozbuy.e_ticket.event.core.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "events")
@Getter
@Setter
public class Event extends AuditableEntity<Long, String> {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Event description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;

    /**
     * Campo geográfico compatível com PostGIS (GEOGRAPHY(POINT, 4326)).
     * Necessita da dependência hibernate-spatial.
     */
    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    @NotNull(message = "Geographic location is required")
    private Point geographicLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    @JsonIgnore  // ⚠️ Ignora esta propriedade na serialização
    private EventCategory category;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore  // ⚠️ Também ignora tickets para evitar outro loop
    private List<EventTicket> tickets = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @Column(name = "min_attendees")
    private Integer minAttendees;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "is_free", nullable = false)
    private Boolean isFree = false;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "total_tickets")
    private Integer totalTickets = 0;

    @Column(name = "available_tickets")
    private Integer availableTickets = 0;

    @Column(name = "sold_tickets")
    private Integer soldTickets = 0;

    @Column(name = "reserved_tickets")
    private Integer reservedTickets = 0;

    public Event() {}

    public Event(String name, String description, Point geographicLocation, EventCategory category, LocalDateTime eventDate) {
        this.name = name;
        this.description = description;
        this.geographicLocation = geographicLocation;
        this.category = category;
        this.eventDate = eventDate;
    }

    public void addTicket(EventTicket ticket) {
        tickets.add(ticket);
        ticket.setEvent(this);
        updateTicketStatistics();
    }

    public void removeTicket(EventTicket ticket) {
        tickets.remove(ticket);
        ticket.setEvent(null);
        updateTicketStatistics();
    }

    public void updateTicketStatistics() {
        this.totalTickets = tickets.stream().mapToInt(EventTicket::getTotalQuantity).sum();
        this.availableTickets = tickets.stream().mapToInt(EventTicket::getAvailableQuantity).sum();
        this.soldTickets = tickets.stream().mapToInt(EventTicket::getSoldQuantity).sum();
        this.reservedTickets = tickets.stream().mapToInt(EventTicket::getReservedQuantity).sum();
    }

    public boolean hasAvailableTickets() {
        return availableTickets > 0;
    }

    public boolean isRegistrationOpen() {
        return registrationDeadline == null || LocalDateTime.now().isBefore(registrationDeadline);
    }

    public boolean isEventActive() {
        return eventDate == null || LocalDateTime.now().isBefore(eventDate);
    }
}