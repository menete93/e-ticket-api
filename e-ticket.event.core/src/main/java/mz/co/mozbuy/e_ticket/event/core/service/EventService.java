// EventService.java
package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.EventRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.EventResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.exceptions.CategoryNotFoundException;
import mz.co.mozbuy.e_ticket.event.core.exceptions.EventNotFoundException;
import mz.co.mozbuy.e_ticket.event.core.mapper.EventMapper;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategory;
import mz.co.mozbuy.e_ticket.event.core.repository.EventCategoryRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final TicketService eventTicketService;
    private final EventMapper eventMapper; // Usar o mapper


    /**
     * Cria um evento sem tickets
     */
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO eventDTO) {
        // Buscar categoria
        EventCategory category = eventCategoryRepository.findById(eventDTO.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(eventDTO.getCategoryId()));

        // Criar evento
        Event event = new Event();
        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setGeographicLocation(eventDTO.getGeographicLocation());
        event.setCategory(category);
        event.setEventDate(eventDTO.getEventDate());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setCoverImageUrl(eventDTO.getCoverImageUrl());
        event.setBannerImageUrl(eventDTO.getBannerImageUrl());
        event.setMaxAttendees(eventDTO.getMaxAttendees());
        event.setMinAttendees(eventDTO.getMinAttendees());
        event.setIsPublic(eventDTO.getIsPublic());
        event.setIsFeatured(eventDTO.getIsFeatured());
        event.setIsFree(eventDTO.getIsFree());
        event.setRegistrationDeadline(eventDTO.getRegistrationDeadline());


        Event savedEvent = eventRepository.save(event);
        log.info("Event created: {} by {}", savedEvent.getName());

        // Criar tickets padrão se solicitado
        if (Boolean.TRUE.equals(eventDTO.getCreateDefaultTickets()) && eventDTO.getMaxAttendees() != null) {
            eventTicketService.createDefaultTickets(savedEvent, eventDTO.getMaxAttendees());
            // Recarregar o evento para incluir os tickets criados
            savedEvent = eventRepository.findById(savedEvent.getId())
                    .orElseThrow(() -> new RuntimeException("Event not found after creation"));
            log.info("Default tickets created for event: {}", savedEvent.getName());
        }

        return eventMapper.toDTO(savedEvent);
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
    public EventResponseDTO updateEvent(Long eventId, EventRequestDTO eventDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        EventCategory category = eventCategoryRepository.findById(eventDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + eventDTO.getCategoryId()));

        // Atualizar campos
        event.setDescription(eventDTO.getDescription());
        event.setGeographicLocation(eventDTO.getGeographicLocation());
        event.setCategory(category);
        event.setEventDate(eventDTO.getEventDate());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setCoverImageUrl(eventDTO.getCoverImageUrl());
        event.setBannerImageUrl(eventDTO.getBannerImageUrl());
        event.setMaxAttendees(eventDTO.getMaxAttendees());
        event.setMinAttendees(eventDTO.getMinAttendees());
        event.setIsPublic(eventDTO.getIsPublic());
        event.setIsFeatured(eventDTO.getIsFeatured());
        event.setIsFree(eventDTO.getIsFree());
        event.setRegistrationDeadline(eventDTO.getRegistrationDeadline());

        // Set updated by

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

    public List<EventResponseDTO> findByState() {

        List<EventResponseDTO> eventResponseDTOS;
        eventResponseDTOS = eventRepository.findByLifeCycleState(LifeCycleState.ACTIVE).stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());

        return eventResponseDTOS;
    }

}