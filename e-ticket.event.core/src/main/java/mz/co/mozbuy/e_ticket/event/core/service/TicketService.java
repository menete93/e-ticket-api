package mz.co.mozbuy.e_ticket.event.core.service;


import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.StrategyType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import mz.co.mozbuy.e_ticket.event.core.exceptions.EventNotFoundException;
import mz.co.mozbuy.e_ticket.event.core.mapper.EventTicketMapper;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.EventTicketRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final EventTicketRepository eventTicketRepository;
    private final EventRepository eventRepository;
    private final EventTicketMapper eventTicketMapper; // Adicionar esta linha

    /**
     * Cria bilhetes durante a criação do evento
     */
    @Transactional
    public List<TicketResponseDTO> createTicketsForEvent(List<TicketRequestDTO> ticketDTOs) {
        return createTickets(ticketDTOs); // ✅ Já retorna List<TicketResponseDTO>

    }

    /**
     * Cria um bilhete individual
     */
    @Transactional
    public List<TicketResponseDTO> createTickets(List<TicketRequestDTO> ticketDTOs) {
        if (ticketDTOs.isEmpty()) return Collections.emptyList();

        Long eventId = ticketDTOs.get(0).getEventId();
        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event"));

        List<TicketResponseDTO> responses = new ArrayList<>();

        for (TicketRequestDTO ticketDTO : ticketDTOs) {
            validateTicketDTO(ticketDTO);

            // ⭐⭐ AGORA O BUILDER FUNCIONA ⭐⭐
            EventTicket ticket = EventTicket.builder()
                    .event(event)
                    .category(ticketDTO.getCategory())
                    .ticketName(ticketDTO.getTicketName())
                    .totalQuantity(ticketDTO.getTotalQuantity())
                    .availableQuantity(ticketDTO.getTotalQuantity())  // Inicial = total
                    .reservedQuantity(0)
                    .soldQuantity(0)
                    .currentPrice(ticketDTO.getPrice())
                    .originalPrice(ticketDTO.getPrice())
                    .description(ticketDTO.getDescription())
                    .benefits(ticketDTO.getBenefits())  // Se existir no DTO
                    .salesStartDate(ticketDTO.getSalesStartDate())  // Se existir
                    .salesEndDate(ticketDTO.getSalesEndDate())  // Se existir
                    .maxTicketsPerUser(ticketDTO.getMaxTicketsPerUser() != null ?
                            ticketDTO.getMaxTicketsPerUser() : 10)
                    .hasDynamicPricing(ticketDTO.getHasDynamicPricing() != null ?
                            ticketDTO.getHasDynamicPricing() : false)
                    .build();

            EventTicket savedTicket = eventTicketRepository.save(ticket);
            event.getTickets().add(savedTicket);
            responses.add(eventTicketMapper.toDTO(savedTicket));
        }

        event.updateTicketStatistics();
        return responses;
    }
    // Método auxiliar para validação
    private void validateTicketDTO(TicketRequestDTO dto) {
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than 0");
        }

        if (dto.getTotalQuantity() == null || dto.getTotalQuantity() <= 0) {
            throw new ValidationException("Total quantity must be greater than 0");
        }

        if (dto.getTicketName() == null || dto.getTicketName().trim().isEmpty()) {
            throw new ValidationException("Ticket name is required");
        }
    }





    public EventTicket createTicketEntity(TicketRequestDTO ticketDTO) {
        Event event = eventRepository.findById(ticketDTO.getEventId()).
                orElseThrow(() -> new EventNotFoundException(ticketDTO.getEventId()));

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

        // Definir datas de venda
        if (ticketDTO.getSalesStartDate() != null) {
            ticket.setSalesStartDate(ticketDTO.getSalesStartDate());
        }
        if (ticketDTO.getSalesEndDate() != null) {
            ticket.setSalesEndDate(ticketDTO.getSalesEndDate());
        }

        return ticket;
    }

    /**
     * Atualiza um bilhete existente
     */
    @Transactional
    public TicketResponseDTO updateTicket(Long eventId, Long ticketId, TicketRequestDTO ticketDTO) {
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
            ticket.updatePrice(ticketDTO.getPrice(),ticketDTO.getChangeReason(), StrategyType.MANUAL.valueOf(), ticketDTO.getStrategyId());
        }


            ticket.setDescription(ticketDTO.getDescription());
        ticket.setBenefits(ticketDTO.getBenefits());
        ticket.setMaxTicketsPerUser(ticketDTO.getMaxTicketsPerUser());
//        ticket.setIsActive(ticketDTO.getIsActive());

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
                createDefaultTicket(event, TicketCategory.NORMAL,
                        (int) (totalCapacity * 0.6), // 60% capacidade
                        new BigDecimal("50.00"),
                        "General Admission",
                        "Standard access to the event area"),

                createDefaultTicket(event, TicketCategory.VIP,
                        (int) (totalCapacity * 0.2), // 20% capacidade
                        new BigDecimal("150.00"),
                        "VIP Experience",
                        "VIP access with premium seating and services"),

                createDefaultTicket(event, TicketCategory.VVIP,
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
        ticket.setLifeCycleState(LifeCycleState.ACTIVE);

        // Definir período de vendas para Early Bird
        if (category == TicketCategory.EARLY_BIRD) {
            ticket.setSalesStartDate(LocalDateTime.now());
            ticket.setSalesEndDate(LocalDateTime.now().plusDays(7)); // 7 dias de early bird
        }

        return ticket;
    }

    public TicketResponseDTO toDTO(EventTicket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
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
        dto.setLifeCycleState(ticket.getLifeCycleState());
        dto.setIsAvailable(ticket.isAvailable());
        dto.setIsSalesPeriodActive(ticket.isSalesPeriodActive());
        dto.setTotalRevenue(ticket.getTotalRevenue());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setCreatedBy(ticket.getCreatedBy());
        dto.setUpdatedBy(ticket.getUpdatedBy());
        return dto;
    }

    /**
     * Busca tickets por evento
     */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByEventId(Long eventId) {
        return eventTicketRepository.findByEventId(eventId).stream()
                .map(this::toDTO) // Agora pode usar this::toDTO pois é público
                .collect(Collectors.toList());
    }

}