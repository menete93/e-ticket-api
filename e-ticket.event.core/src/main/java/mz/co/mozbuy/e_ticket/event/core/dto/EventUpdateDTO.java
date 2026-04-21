// EventUpdateDTO.java
package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data  // ← Lombok já gera getters/setters
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateDTO {

    private String name;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxAttendees;
    private Integer minAttendees;
    private Boolean isPublic;
    private Boolean isFeatured;
    private Boolean isFree;
    private LocalDateTime registrationDeadline;
}