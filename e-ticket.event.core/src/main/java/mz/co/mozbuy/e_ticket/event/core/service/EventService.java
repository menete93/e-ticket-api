package mz.co.mozbuy.e_ticket.event.core.service;


import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.EventRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.EventResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategory;
import mz.co.mozbuy.e_ticket.event.core.repository.EventCategoryRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventTicketService eventTicketService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO eventDTO, String createdBy) {
        EventCategory category = eventCategoryRepository.findById(eventDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + eventDTO.getCategoryId()));

        Point location = createPoint(eventDTO.getLatitude(), eventDTO.getLongitude());

        Event event = new Event();
        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setGeographicLocation(location);
        event.setCategory(category);
        event.setEventDate(LocalDateTime.parse(eventDTO.getEventDate()));
        event.setCreatedBy(createdBy);

        // Definir propriedades opcionais
        if (eventDTO.getStartTime() != null) {
            event.setStartTime(LocalDateTime.parse(eventDTO.getStartTime()));
        }
        if (eventDTO.getEndTime() != null) {
            event.setEndTime(LocalDateTime.parse(eventDTO.getEndTime()));
        }
        if (eventDTO.getRegistrationDeadline() != null) {
            event.setRegistrationDeadline(LocalDateTime.parse(eventDTO.getRegistrationDeadline()));
        }

        event.setCoverImageUrl(eventDTO.getCoverImageUrl());
        event.setBannerImageUrl(eventDTO.getBannerImageUrl());
        event.setMaxAttendees(eventDTO.getMaxAttendees());
        event.setMinAttendees(eventDTO.getMinAttendees());
        event.setIsPublic(eventDTO.getIsPublic());
        event.setIsFeatured(eventDTO.getIsFeatured());
        event.setIsFree(eventDTO.getIsFree());

        Event savedEvent = eventRepository.save(event);

        // Criar bilhetes baseados no DTO ou criar padrão
        if (eventDTO.getTickets() != null && !eventDTO.getTickets().isEmpty()) {
            eventTicketService.createTicketsForEvent(savedEvent, eventDTO.getTickets());
        } else if (eventDTO.getMaxAttendees() != null) {
            // Criar bilhetes padrão baseado na capacidade
            eventTicketService.createDefaultTickets(savedEvent, eventDTO.getMaxAttendees());
        }

        // Recarregar o evento com os bilhetes
        Event completeEvent = eventRepository.findById(savedEvent.getId()).orElse(savedEvent);
        return toDTO(completeEvent);
    }

    private Point createPoint(Double latitude, Double longitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private EventResponseDTO toDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());

        if (event.getGeographicLocation() != null) {
            dto.setLatitude(event.getGeographicLocation().getY());
            dto.setLongitude(event.getGeographicLocation().getX());
        }

        // Converter categoria para DTO
        // dto.setCategory(toCategoryDTO(event.getCategory()));

        dto.setEventDate(event.getEventDate());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setRegistrationDeadline(event.getRegistrationDeadline());
        dto.setCoverImageUrl(event.getCoverImageUrl());
        dto.setBannerImageUrl(event.getBannerImageUrl());
        dto.setMaxAttendees(event.getMaxAttendees());
        dto.setMinAttendees(event.getMinAttendees());
        dto.setIsPublic(event.getIsPublic());
        dto.setIsFeatured(event.getIsFeatured());
        dto.setIsFree(event.getIsFree());
        dto.setTotalTickets(event.getTotalTickets());
        dto.setAvailableTickets(event.getAvailableTickets());
        dto.setSoldTickets(event.getSoldTickets());
        dto.setReservedTickets(event.getReservedTickets());
        dto.setHasAvailableTickets(event.hasAvailableTickets());
        dto.setIsRegistrationOpen(event.isRegistrationOpen());
        dto.setIsEventActive(event.isEventActive());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setCreatedBy(event.getCreatedBy());
        dto.setUpdatedBy(event.getUpdatedBy());

        return dto;
    }
}