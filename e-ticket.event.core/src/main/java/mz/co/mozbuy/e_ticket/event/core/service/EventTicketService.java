package mz.co.mozbuy.e_ticket.event.core.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.EventTicketRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.EventTicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.EventTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventTicketService {

    private final EventTicketRepository eventTicketRepository;
    private final EventRepository eventRepository;

    /**
     * Cria bilhetes durante a criação do evento
     */
    @Transactional
    public List<EventTicket> createTicketsForEvent(Event event, List<EventTicketRequestDTO> ticketDTOs) {
        return ticketDTOs.stream()
                .map(ticketDTO -> createTicket(event, ticketDTO))
                .collect(Collectors.toList());
    }

    /**
     * Cria um bilhete individual
     */
    @Transactional
    public EventTicketResponseDTO createTicket(EventTicketRequestDTO ticketDTO) {
        Event event = eventRepository.findById(ticketDTO.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + ticketDTO.getEventId()));

        EventTicket ticket = createTicket(event, ticketDTO);
        EventTicket savedTicket = eventTicketRepository.save(ticket);

        event.addTicket(savedTicket);
        eventRepository.save(event);

        log.info("Ticket created for event {}: {} - {}", ticketDTO.getEventId(), ticketDTO.getCategory(), ticketDTO.getTicketName());
        return toDTO(savedTicket);
    }

    public EventTicket createTicket(Event event, EventTicketRequestDTO ticketDTO) {
        EventTicket ticket = new EventTicket();
        ticket.setEvent(event);
        ticket.setCategory(ticketDTO.getCategory());
        ticket.setTicketName(ticketDTO.getTicketName());
        ticket.setTotalQuantity(ticketDTO.getTotalQuantity());
        ticket.setAvailableQuantity(ticketDTO.getTotalQuantity());

        // CORREÇÃO AQUI: usar setCurrentPrice e setOriginalPrice
        BigDecimal price = ticketDTO.getPrice() != null ? ticketDTO.getPrice() : BigDecimal.ZERO;
        ticket.setCurrentPrice(price);
        ticket.setOriginalPrice(price);

        ticket.setDescription(ticketDTO.getDescription());
        ticket.setBenefits(ticketDTO.getBenefits());
        ticket.setMaxTicketsPerUser(ticketDTO.getMaxTicketsPerUser());
        ticket.setIsActive(ticketDTO.getIsActive());

        // Definir datas de venda
        if (ticketDTO.getSalesStartDate() != null) {
            ticket.setSalesStartDate(LocalDateTime.parse(ticketDTO.getSalesStartDate()));
        }
        if (ticketDTO.getSalesEndDate() != null) {
            ticket.setSalesEndDate(LocalDateTime.parse(ticketDTO.getSalesEndDate()));
        }

        return ticket;
    }

    /**
     * Atualiza um bilhete existente
     */
    @Transactional
    public EventTicketResponseDTO updateTicket(Long eventId, Long ticketId, EventTicketRequestDTO ticketDTO) {
        EventTicket ticket = eventTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (!ticket.getEvent().getId().equals(eventId)) {
            throw new RuntimeException("Ticket does not belong to the specified event");
        }

        // Verificar se pode alterar a quantidade total
        if (!ticketDTO.getTotalQuantity().equals(ticket.getTotalQuantity())) {
            int difference = ticketDTO.getTotalQuantity() - ticket.getTotalQuantity();
            if (difference < 0 && Math.abs(difference) > ticket.getAvailableQuantity()) {
                throw new RuntimeException("Cannot reduce total quantity below sold + reserved tickets");
            }
            ticket.setAvailableQuantity(ticket.getAvailableQuantity() + difference);
        }

        ticket.setCategory(ticketDTO.getCategory());
        ticket.setTicketName(ticketDTO.getTicketName());
        ticket.setTotalQuantity(ticketDTO.getTotalQuantity());

        // CORREÇÃO AQUI: atualizar currentPrice
        if (ticketDTO.getPrice() != null && !ticketDTO.getPrice().equals(ticket.getCurrentPrice())) {
            ticket.updatePrice(ticketDTO.getPrice(), "Manual price update");
        }

        ticket.setDescription(ticketDTO.getDescription());
        ticket.setBenefits(ticketDTO.getBenefits());
        ticket.setMaxTicketsPerUser(ticketDTO.getMaxTicketsPerUser());
        ticket.setIsActive(ticketDTO.getIsActive());

        EventTicket updatedTicket = eventTicketRepository.save(ticket);
        updatedTicket.getEvent().updateTicketStatistics();
        eventRepository.save(updatedTicket.getEvent());

        log.info("Ticket updated: {} - {}", ticketDTO.getCategory(), ticketDTO.getTicketName());
        return toDTO(updatedTicket);
    }

    /**
     * Cria categorias de bilhetes padrão para um evento
     */
    @Transactional
    public List<EventTicket> createDefaultTickets(Event event, Integer totalCapacity) {
        List<EventTicket> defaultTickets = List.of(
                createDefaultTicket(event, TicketCategory.GENERAL_ADMISSION,
                        (int) (totalCapacity * 0.6), // 60% capacidade
                        new BigDecimal("50.00"),
                        "General Admission",
                        "Standard access to the event area"),

                createDefaultTicket(event, TicketCategory.VIP,
                        (int) (totalCapacity * 0.2), // 20% capacidade
                        new BigDecimal("150.00"),
                        "VIP Experience",
                        "VIP access with premium seating and services"),

                createDefaultTicket(event, TicketCategory.V_VIP,
                        (int) (totalCapacity * 0.1), // 10% capacidade
                        new BigDecimal("300.00"),
                        "VVIP Premium",
                        "Ultimate experience with backstage access"),

                createDefaultTicket(event, TicketCategory.EARLY_BIRD,
                        (int) (totalCapacity * 0.1), // 10% capacidade
                        new BigDecimal("35.00"),
                        "Early Bird Special",
                        "Limited time discounted tickets")
        );

        return eventTicketRepository.saveAll(defaultTickets);
    }

    private EventTicket createDefaultTicket(Event event, TicketCategory category,
                                            Integer quantity, BigDecimal price,
                                            String name, String description) {
        EventTicket ticket = new EventTicket();
        ticket.setEvent(event);
        ticket.setCategory(category);
        ticket.setTicketName(name);
        ticket.setTotalQuantity(quantity);
        ticket.setAvailableQuantity(quantity);

        // CORREÇÃO AQUI: usar currentPrice e originalPrice
        ticket.setCurrentPrice(price);
        ticket.setOriginalPrice(price);

        ticket.setDescription(description);
        ticket.setIsActive(true);

        // Definir período de vendas para Early Bird
        if (category == TicketCategory.EARLY_BIRD) {
            ticket.setSalesStartDate(LocalDateTime.now());
            ticket.setSalesEndDate(LocalDateTime.now().plusDays(7)); // 7 dias de early bird
        }

        return ticket;
    }

    private EventTicketResponseDTO toDTO(EventTicket ticket) {
        EventTicketResponseDTO dto = new EventTicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setCategory(ticket.getCategory());
        dto.setTicketName(ticket.getTicketName());
        dto.setTotalQuantity(ticket.getTotalQuantity());
        dto.setAvailableQuantity(ticket.getAvailableQuantity());
        dto.setReservedQuantity(ticket.getReservedQuantity());
        dto.setSoldQuantity(ticket.getSoldQuantity());

        // CORREÇÃO AQUI: usar getCurrentPrice() no DTO
        dto.setPrice(ticket.getCurrentPrice());

        dto.setDescription(ticket.getDescription());
        dto.setBenefits(ticket.getBenefits());
        dto.setSalesStartDate(ticket.getSalesStartDate());
        dto.setSalesEndDate(ticket.getSalesEndDate());
        dto.setMaxTicketsPerUser(ticket.getMaxTicketsPerUser());
        dto.setIsActive(ticket.getIsActive());
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