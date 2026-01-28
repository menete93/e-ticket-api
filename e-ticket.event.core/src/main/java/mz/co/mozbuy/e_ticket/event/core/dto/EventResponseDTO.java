// EventResponseDTO.java
package mz.co.mozbuy.e_ticket.event.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.common.audit.LifeCycleState;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String coverImageUrl;
    private String bannerImageUrl;
    private Integer maxAttendees;
    private Integer minAttendees;
    private Boolean isPublic;
    private Boolean isFeatured;
    private Boolean isFree;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDeadline;
    private Integer totalTickets;
    private Integer availableTickets;
    private Integer soldTickets;
    private Integer reservedTickets;
    private List<TicketResponseDTO> tickets = new ArrayList<>();
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
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

    public void setLifeCycleState(LifeCycleState lifeCycleState) {
    }
}