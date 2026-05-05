package mz.co.mozbuy.e_ticket.event.core.mapper;

import mz.co.mozbuy.e_ticket.event.core.dto.TicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary  // Substitui o mapper problemático
public class SafeEventTicketMapper {

    public TicketResponseDTO toDTO(EventTicket ticket) {
        if (ticket == null) return null;

        TicketResponseDTO dto = new TicketResponseDTO();

        // ✅ CAMPOS SEGUROS (nunca falham)
        try { dto.setId(ticket.getId()); } catch (Exception e) { /* ignora */ }
        try { dto.setTicketName(ticket.getTicketName()); } catch (Exception e) { /* ignora */ }
        try { dto.setCategory(ticket.getCategory()); } catch (Exception e) { /* ignora */ }

        // ✅ Campos numéricos básicos
        try { dto.setTotalQuantity(ticket.getTotalQuantity()); } catch (Exception e) { /* ignora */ }
        try { dto.setAvailableQuantity(ticket.getAvailableQuantity()); } catch (Exception e) { /* ignora */ }
        try { dto.setReservedQuantity(ticket.getReservedQuantity()); } catch (Exception e) { /* ignora */ }
        try { dto.setSoldQuantity(ticket.getSoldQuantity()); } catch (Exception e) { /* ignora */ }
        try { dto.setPrice(ticket.getCurrentPrice()); } catch (Exception e) { /* ignora */ }

        // ✅ Campos simples
        try { dto.setDescription(ticket.getDescription()); } catch (Exception e) { /* ignora */ }
        try { dto.setMaxTicketsPerUser(ticket.getMaxTicketsPerUser()); } catch (Exception e) { /* ignora */ }
        try { dto.setLifeCycleState(ticket.getState()); } catch (Exception e) { /* ignora */ }

        // ⚠️ CAMPOS PERIGOSOS (pulam se falharem)
        try {
            dto.setSalesStartDate(ticket.getSalesStartDate());
        } catch (Exception e) {
            System.out.println("SalesStartDate falhou: " + e.getMessage());
        }

        try {
            dto.setSalesEndDate(ticket.getSalesEndDate());
        } catch (Exception e) {
            System.out.println("SalesEndDate falhou: " + e.getMessage());
        }

        // ❌ NUNCA tente estes (causam HibernateException)
         dto.setBenefits(ticket.getBenefits());  // Coleção lazy
         dto.setIsAvailable(ticket.isAvailable());  // Método de negócio
         dto.setIsSalesPeriodActive(ticket.isSalesPeriodActive());  // Método
         dto.setTotalRevenue(ticket.getTotalRevenue());  // Cálculo complexo
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setCreatedBy(ticket.getCreatedBy());
        dto.setUpdatedBy(ticket.getUpdatedBy());
        return dto;
    }
}