// EventService.java
package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.*;
import mz.co.mozbuy.e_ticket.event.core.exceptions.*;
import mz.co.mozbuy.e_ticket.event.core.mapper.EventMapper;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategory;
import mz.co.mozbuy.e_ticket.event.core.model.Organizer;
import mz.co.mozbuy.e_ticket.event.core.repository.EventCategoryRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.OrganizerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final TicketService eventTicketService;
    private final EventMapper eventMapper; // Usar o mapper
    private final OrganizerRepository organizerRepository;


    /**
     * Cria um evento sem tickets
     */


    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO eventDTO) {
        log.info("Creating event: {} for organizer: {}",
                eventDTO.getName(), eventDTO.getUserId());

        // 1. Buscar organizador
        Organizer organizer = organizerRepository.findByUserId(eventDTO.getUserId())
                .orElseThrow(() -> new OrganizerNotFoundException(eventDTO.getUserId()));

        // 2. Buscar categoria
        EventCategory category = eventCategoryRepository.findById(eventDTO.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(eventDTO.getCategoryId()));

        // 3. Validar se organizador pode criar eventos
        if (!organizer.canCreateEvents()) {
            throw new OrganizerNotAllowedException(
                    "Organizer %s cannot create events. Status: %s",
                            organizer.getName(),"status"+ organizer.getLifeCycleState().getDbValue());

        }

        // 4. Criar evento usando um método auxiliar (ou builder)
        Event event = createEventEntity(eventDTO, organizer, category);

        // 5. Verificar e aplicar trial se elegível
        applyTrialIfEligible(event, organizer, eventDTO);

        // 6. Salvar evento
        Event savedEvent = eventRepository.save(event);

        // 7. Criar tickets padrão se solicitado
        if (Boolean.TRUE.equals(eventDTO.getCreateDefaultTickets())) {
            createDefaultTicketsIfNeeded(savedEvent, eventDTO);

            final Long eventId = savedEvent.getId();

            savedEvent = eventRepository.findByIdWithTickets(eventId)
                    .orElseThrow(() -> new EventNotFoundException(eventId));
        }


        // 8. Atualizar contador de eventos do organizador
        organizer.incrementEventsCreated();
        organizerRepository.save(organizer);

        log.info("Event created successfully: {} (ID: {}) by organizer: {}",
                savedEvent.getName(), savedEvent.getId(), organizer.getName());

        return eventMapper.toDTO(savedEvent);
    }

    private Event createEventEntity(EventRequestDTO dto, Organizer organizer, EventCategory category) {
        Event event = new Event();
        event.setName(dto.getName());
        event.setOrganizer(organizer);
        event.setCategory(category);
        event.setDescription(dto.getDescription());
        event.setGeographicLocation(dto.getGeographicLocation());
        event.setEventDate(dto.getEventDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setCoverImageUrl(dto.getCoverImageUrl());
        event.setBannerImageUrl(dto.getBannerImageUrl());
        event.setMaxAttendees(dto.getMaxAttendees());
        event.setMinAttendees(dto.getMinAttendees());
        event.setIsPublic(dto.getIsPublic());
        event.setIsFeatured(dto.getIsFeatured());
        event.setIsFree(dto.getIsFree());
        event.setRegistrationDeadline(dto.getRegistrationDeadline());

        // Configuração de pricing específica do evento (se fornecida)
        if (dto.getEventCommissionRate() != null) {
            event.setEventCommissionRate(dto.getEventCommissionRate());
        }
        if (dto.getEventFlatFee() != null) {
            event.setEventFlatFee(dto.getEventFlatFee());
        }

        return event;
    }

    private void applyTrialIfEligible(Event event, Organizer organizer, EventRequestDTO dto) {
        // Se organizador tem trial disponível E evento é marcado como trial
        if (organizer.isEventEligibleForTrial() &&
                Boolean.TRUE.equals(dto.getIsTrialEvent())) {
            event.setIsTrialEvent(true);
            log.debug("Event marked as trial. Trials remaining: {}",
                    organizer.getTrialEventsRemaining());
        }
    }

    private void createDefaultTicketsIfNeeded(Event event, EventRequestDTO dto) {
        if (dto.getMaxAttendees() == null || dto.getMaxAttendees() <= 0) {
            throw new IllegalArgumentException(
                    "maxAttendees must be provided and greater than 0 for default tickets");
        }

        try {
            eventTicketService.createDefaultTickets(event, dto.getMaxAttendees());
            log.info("Default tickets created for event: {} ({} tickets)",
                    event.getName(), dto.getMaxAttendees());
        } catch (Exception e) {
            log.error("Failed to create default tickets for event: {}", event.getId(), e);
            throw new TicketCreationException("Failed to create default tickets", e);
        }
    }
    /**
     * Busca evento por ID
     */
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        return eventMapper.toDTO(event);
    }

    /**
     * Atualiza um evento
     */
    @Transactional
    public EventResponseDTO updateEvent(Long eventId, EventUpdateDTO eventUpdate) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Atualiza apenas campos não nulos
        if (eventUpdate.getName() != null) {
            event.setName(eventUpdate.getName());
        }
        if (eventUpdate.getDescription() != null) {
            event.setDescription(eventUpdate.getDescription());
        }
        if (eventUpdate.getEventDate() != null) {
            event.setEventDate(eventUpdate.getEventDate());
        }
        if (eventUpdate.getStartTime() != null) {
            event.setStartTime(eventUpdate.getStartTime());
        }
        if (eventUpdate.getEndTime() != null) {
            event.setEndTime(eventUpdate.getEndTime());
        }
        if (eventUpdate.getMaxAttendees() != null) {
            event.setMaxAttendees(eventUpdate.getMaxAttendees());
        }
        if (eventUpdate.getMinAttendees() != null) {
            event.setMinAttendees(eventUpdate.getMinAttendees());
        }
        if (eventUpdate.getIsPublic() != null) {
            event.setIsPublic(eventUpdate.getIsPublic());
        }
        if (eventUpdate.getIsFeatured() != null) {
            event.setIsFeatured(eventUpdate.getIsFeatured());
        }
        if (eventUpdate.getIsFree() != null) {
            event.setIsFree(eventUpdate.getIsFree());
        }
        if (eventUpdate.getRegistrationDeadline() != null) {
            event.setRegistrationDeadline(eventUpdate.getRegistrationDeadline());
        }

        event.setUpdatedAt(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated: {} by {}", updatedEvent.getName());

        return eventMapper.toDTO(updatedEvent);
    }
    /**
     * Lista todos os eventos
     */
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents() {

        List<EventResponseDTO> eventResponseDTOS;
        eventResponseDTOS = eventRepository.findAll().stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());

        return eventResponseDTOS;
    }
    /**
     * Deleta um evento (soft delete se estiver usando)
     */
    @Transactional
    public void deleteEvent(Long eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Verificar se há tickets vendidos
        if (event.getSoldTickets() > 0) {
            throw new RuntimeException("Cannot delete event with sold tickets");
        }

        eventRepository.delete(event);
        log.info("Event deleted: {} by {}", event.getName(), username);
    }

    /**
     * Converte Entity para DTO
     */
//    private EventResponseDTO toDTO(Event event) {
//        EventResponseDTO dto = new EventResponseDTO();
//        dto.setId(event.getId());
//        dto.setName(event.getName());
//        dto.setDescription(event.getDescription());
//        dto.setGeographicLocation(event.getGeographicLocation());
//        dto.setCategory(event.getCategory());
//        dto.setEventDate(event.getEventDate());
//        dto.setStartTime(event.getStartTime());
//        dto.setEndTime(event.getEndTime());
//        dto.setCoverImageUrl(event.getCoverImageUrl());
//        dto.setBannerImageUrl(event.getBannerImageUrl());
//        dto.setMaxAttendees(event.getMaxAttendees());
//        dto.setMinAttendees(event.getMinAttendees());
//        dto.setIsPublic(event.getIsPublic());
//        dto.setIsFeatured(event.getIsFeatured());
//        dto.setIsFree(event.getIsFree());
//        dto.setRegistrationDeadline(event.getRegistrationDeadline());
//        dto.setTotalTickets(event.getTotalTickets());
//        dto.setAvailableTickets(event.getAvailableTickets());
//        dto.setSoldTickets(event.getSoldTickets());
//        dto.setReservedTickets(event.getReservedTickets());
//        dto.setCreatedAt(event.getCreatedAt());
//        dto.setUpdatedAt(event.getUpdatedAt());
//        dto.setCreatedBy(event.getCreatedBy());
//        dto.setUpdatedBy(event.getUpdatedBy());
//
//        // Converter tickets se existirem
//        if (event.getTickets() != null && !event.getTickets().isEmpty()) {
//            dto.setTickets(event.getTickets().stream()
//                    .map(eventTicketService::toDTO)
//                    .collect(Collectors.toList()));
//        }
//
//        return dto;
//    }

//    public List<EventResponseDTO> findByState() {
//        // Agora use a query JPQL com JOIN FETCH
//        List<Event> events = eventRepository.findActiveNativeCast();
//
//        if (events.isEmpty()) {
//            // Teste sem JOIN primeiro
//            List<Event> eventsWithoutTickets = eventRepository.findActiveNativeCast();
//            System.out.println("Eventos sem tickets: " + eventsWithoutTickets.size());
//
//            // Verifique se há algum problema com a entidade EventTicket
//            for (Event event : eventsWithoutTickets) {
//                System.out.println("Evento ID: " + event.getId() + ", Nome: " + event.getName());
//                try {
//                    // Tente forçar o carregamento dos tickets
//                    Hibernate.initialize(event.getTickets());
//                    System.out.println("  Tickets: " + event.getTickets().size());
//                } catch (Exception e) {
//                    System.out.println("  ERRO ao carregar tickets: " + e.getMessage());
//                }
//            }
//        }
//
//        return events.stream()
//                .map(eventMapper::toDTO)
//                .collect(Collectors.toList());
//    }


    public List<EventResponseDTO> findByState() {
        System.out.println("=== BUSCANDO EVENTOS ATIVOS ===");

        // Use o método COM JOIN FETCH para carregar tickets
        List<Event> events = eventRepository.findByLifeCycleState(LifeCycleState.ACTIVE);
        System.out.println("Eventos encontrados com tickets: " + events.size());

        if (!events.isEmpty()) {
            Event primeiro = events.get(0);
            System.out.println("Primeiro evento: " + primeiro.getName());
            System.out.println("Tickets carregados: " + primeiro.getTickets().size());
        }

        if (events.isEmpty()) {
            throw new EventNotFoundException();
        }

        // Converta para DTO
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }





        public List<EventWithTicketsDTO> getActiveEventsWithTickets() {
            // 1. Busca projeção
            List<EventRepository.EventWithTicketsProjection> projections =
                    eventRepository.findActiveEventsWithTickets();

            // 2. Agrupa por evento
            Map<Long, EventWithTicketsDTO> eventMap = new LinkedHashMap<>();

            for (EventRepository.EventWithTicketsProjection p : projections) {
                Long eventId = p.getEventId();

                if (!eventMap.containsKey(eventId)) {
                    EventSimpleDTO eventDto = new EventSimpleDTO();
                    eventDto.setId(eventId);
                    eventDto.setName(p.getEventName());
                    // Busca outros campos se necessário

                    EventWithTicketsDTO dto = new EventWithTicketsDTO();
                    dto.setEvent(eventDto);
                    dto.setTickets(new ArrayList<>());
                    eventMap.put(eventId, dto);
                }

                // Adiciona ticket se existir
                if (p.getTicketId() != null) {
                    TicketSimpleDTO ticketDto = new TicketSimpleDTO();
                    ticketDto.setId(p.getTicketId());
                    ticketDto.setTicketName(p.getTicketName());
                    ticketDto.setPrice(p.getTicketPrice());

                    eventMap.get(eventId).getTickets().add(ticketDto);
                }
            }

            return new ArrayList<>(eventMap.values());

    }



    public List<EventResponseDTO> findByStateAndOrganizerId(String referenceId) {

        // Use o método COM JOIN FETCH para carregar tickets
        List<Event> events = eventRepository.findActiveEventsByOrganizerWithTickets(referenceId);
        System.out.println("Eventos encontrados para o organizador com tickets: " + events.size());

        if (!events.isEmpty()) {
            Event primeiro = events.get(0);
            System.out.println("Primeiro evento: " + primeiro.getName());
            System.out.println("Tickets carregados: " + primeiro.getTickets().size());
        }

        if (events.isEmpty()) {
            throw new EventNotFoundException();
        }

        // Converta para DTO
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public EventResponseDTO cancelEvent(Long eventId, CancelEventRequestDTO cancelRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Verificar se já foi cancelado
        if (Boolean.TRUE.equals(event.getIsCancelled())) {
            throw new IllegalStateException("Evento já está cancelado");
        }

        // Verificar se o evento já ocorreu
        if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível cancelar um evento que já ocorreu");
        }

        // Cancelar o evento
        event.setIsCancelled(true);
        event.setCancelledAt(LocalDateTime.now());
        event.setCancelReason(cancelRequest.getReason());

        // Se houver tickets vendidos e organizador optou por reembolsar
        if (Boolean.TRUE.equals(cancelRequest.getRefundTickets()) && event.getSoldTickets() > 0) {
            event.setRefundProcessed(true);
            // Disparar processo de reembolso assíncrono  POR IMPLEMENTAR UMA TASK
//            refundService.processRefundsForEvent(eventId);
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event cancelled: {} (ID: {}) - Reason: {}",
                event.getName(), event.getId(), cancelRequest.getReason());

        return eventMapper.toDTO(updatedEvent);
    }
}