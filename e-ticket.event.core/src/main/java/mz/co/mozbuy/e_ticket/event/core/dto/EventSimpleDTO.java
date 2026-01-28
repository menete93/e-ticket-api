package mz.co.mozbuy.e_ticket.event.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventSimpleDTO {
    private Long id;
    private String name;
    private String description;
    private LifeCycleState lifeCycleState;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    // APENAS getters/setters
}