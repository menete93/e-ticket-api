// EventMapper.java
package mz.co.mozbuy.e_ticket.event.core.mapper;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.EventResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final EventTicketMapper eventTicketMapper;

    public EventResponseDTO toDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setGeographicLocation(event.getGeographicLocation());
        dto.setCategory(event.getCategory());
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

        // Converter tickets se existirem
        if (event.getTickets() != null && !event.getTickets().isEmpty()) {
            List<TicketResponseDTO> ticketDTOs = event.getTickets().stream()
                    .map(eventTicketMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setTickets(ticketDTOs);
        }

        return dto;
    }
}