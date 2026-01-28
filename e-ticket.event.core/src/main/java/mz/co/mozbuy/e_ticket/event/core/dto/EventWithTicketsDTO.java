package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventWithTicketsDTO {
    private EventSimpleDTO event;
    private List<TicketSimpleDTO> tickets;
}
