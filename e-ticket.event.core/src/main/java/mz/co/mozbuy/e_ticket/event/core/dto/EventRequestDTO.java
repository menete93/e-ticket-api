// EventRequestDTO.java
package mz.co.mozbuy.e_ticket.event.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDTO {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String name;

    @NotBlank(message = "Event description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "Geographic location is required")
    private Point geographicLocation;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String coverImageUrl;
    private String bannerImageUrl;
    private Integer maxAttendees;
    private Integer minAttendees;
    private Boolean isPublic = true;
    private Boolean isFeatured = false;
    private Boolean isFree = false;
    private LocalDateTime registrationDeadline;

    // Flag para criar tickets padrão automaticamente
    private Boolean createDefaultTickets = false;
}