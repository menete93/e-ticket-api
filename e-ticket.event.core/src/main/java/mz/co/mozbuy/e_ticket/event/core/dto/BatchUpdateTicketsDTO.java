// BatchUpdateTicketsDTO.java
package mz.co.mozbuy.e_ticket.event.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateTicketsDTO {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @Valid
    private List<TicketQuantityUpdateDTO> tickets = new ArrayList<>();

    private String globalChangeReason;
}