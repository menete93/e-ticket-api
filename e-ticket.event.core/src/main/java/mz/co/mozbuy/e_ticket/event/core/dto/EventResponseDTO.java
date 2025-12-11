// EventResponseDTO.java
package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategory;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Point geographicLocation;
    private EventCategory category;
    private LocalDateTime eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String coverImageUrl;
    private String bannerImageUrl;
    private Integer maxAttendees;
    private Integer minAttendees;
    private Boolean isPublic;
    private Boolean isFeatured;
    private Boolean isFree;
    private LocalDateTime registrationDeadline;
    private Integer totalTickets;
    private Integer availableTickets;
    private Integer soldTickets;
    private Integer reservedTickets;
    private List<TicketResponseDTO> tickets = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Métodos auxiliares
    public boolean hasAvailableTickets() {
        return availableTickets != null && availableTickets > 0;
    }

    public boolean isRegistrationOpen() {
        return registrationDeadline == null || LocalDateTime.now().isBefore(registrationDeadline);
    }

    public boolean isEventActive() {
        return eventDate == null || LocalDateTime.now().isBefore(eventDate);
    }
}