// EventTicketMapper.java
package mz.co.mozbuy.e_ticket.event.core.mapper;

import mz.co.mozbuy.e_ticket.event.core.dto.TicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import org.springframework.stereotype.Component;

@Component
public class EventTicketMapper {

    public TicketResponseDTO toDTO(EventTicket ticket) {
        if (ticket == null) {
            return null;
        }

        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setCategory(ticket.getCategory());
        dto.setTicketName(ticket.getTicketName());
        dto.setTotalQuantity(ticket.getTotalQuantity());
        dto.setAvailableQuantity(ticket.getAvailableQuantity());
        dto.setReservedQuantity(ticket.getReservedQuantity());
        dto.setSoldQuantity(ticket.getSoldQuantity());
        dto.setPrice(ticket.getCurrentPrice());
        dto.setDescription(ticket.getDescription());
        dto.setBenefits(ticket.getBenefits());
        dto.setSalesStartDate(ticket.getSalesStartDate());
        dto.setSalesEndDate(ticket.getSalesEndDate());
        dto.setMaxTicketsPerUser(ticket.getMaxTicketsPerUser());
        dto.setState(ticket.getState());
        dto.setIsAvailable(ticket.isAvailable());
        dto.setIsSalesPeriodActive(ticket.isSalesPeriodActive());
        dto.setTotalRevenue(ticket.getTotalRevenue());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setCreatedBy(ticket.getCreatedBy());
        dto.setUpdatedBy(ticket.getUpdatedBy());
        return dto;
    }
}