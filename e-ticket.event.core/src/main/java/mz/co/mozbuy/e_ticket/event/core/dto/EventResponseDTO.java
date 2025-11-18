package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Double latitude;
    private Double longitude;
    private EventCategoryDTO category;
    private List<EventTicketResponseDTO> tickets = new ArrayList<>();
    private LocalDateTime eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationDeadline;
    private String coverImageUrl;
    private String bannerImageUrl;
    private Integer maxAttendees;
    private Integer minAttendees;
    private Boolean isPublic;
    private Boolean isFeatured;
    private Boolean isFree;
    private Integer totalTickets;
    private Integer availableTickets;
    private Integer soldTickets;
    private Integer reservedTickets;
    private Boolean hasAvailableTickets;
    private Boolean isRegistrationOpen;
    private Boolean isEventActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}