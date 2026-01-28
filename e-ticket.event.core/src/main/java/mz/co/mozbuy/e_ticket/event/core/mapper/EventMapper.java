// EventMapper.java
package mz.co.mozbuy.e_ticket.event.core.mapper;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.EventResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class EventMapper {

    private final SafeEventTicketMapper safeEventTicketMapper;  // ← Use o seguro

    public EventResponseDTO toDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();

        // Campos do evento
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setGeographicLocation(event.getGeographicLocation());
//        dto.setCategory(event.getCategory());
        dto.setEventDate(event.getEventDate());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setCoverImageUrl(event.getCoverImageUrl());
        dto.setBannerImageUrl(event.getBannerImageUrl());
        dto.setMaxAttendees(event.getMaxAttendees());
        dto.setMinAttendees(event.getMinAttendees());
        dto.setIsPublic(event.getIsPublic());
       dto.setIsFeatured(event.getIsFeatured());
        dto.setIsFree(event.getIsFree());
        dto.setRegistrationDeadline(event.getRegistrationDeadline());
        dto.setTotalTickets(event.getTotalTickets());
        dto.setAvailableTickets(event.getAvailableTickets());
        dto.setSoldTickets(event.getSoldTickets());
        dto.setReservedTickets(event.getReservedTickets());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setCreatedBy(event.getCreatedBy());
        dto.setUpdatedBy(event.getUpdatedBy());

        // ... outros campos

        // Tickets com tratamento robusto
        dto.setTickets(extractTicketsSafely(event));

        return dto;
    }

    private List<TicketResponseDTO> extractTicketsSafely(Event event) {
        List<TicketResponseDTO> ticketDTOs = new ArrayList<>();

        if (event == null || event.getTickets() == null) {
            return ticketDTOs;
        }

        System.out.println("Processando " + event.getTickets().size() + " tickets...");

        for (EventTicket ticket : event.getTickets()) {
            try {
                TicketResponseDTO ticketDTO = safeEventTicketMapper.toDTO(ticket);
                if (ticketDTO != null) {
                    ticketDTOs.add(ticketDTO);
                }
            } catch (Exception e) {
                System.out.println("ERRO no ticket ID " +
                        (ticket != null ? ticket.getId() : "null") +
                        ": " + e.getClass().getSimpleName() + " - " + e.getMessage());

                // Cria DTO mínimo
                TicketResponseDTO fallbackDTO = new TicketResponseDTO();
                if (ticket != null && ticket.getId() != null) {
                    fallbackDTO.setId(ticket.getId());
                    fallbackDTO.setTicketName("Ticket " + ticket.getId());
                    ticketDTOs.add(fallbackDTO);
                }
            }
        }

        return ticketDTOs;
    }
}