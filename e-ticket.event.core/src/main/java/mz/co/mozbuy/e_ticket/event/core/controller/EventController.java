// EventController.java
package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.*;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import mz.co.mozbuy.e_ticket.event.core.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventRepository eventRepository;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO eventDTO) {

        EventResponseDTO createdEvent = eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent( @PathVariable("eventId") Long eventId) {
        EventResponseDTO event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody EventUpdateDTO eventDTO
         ) {

        EventResponseDTO updatedEvent = eventService.updateEvent(eventId, eventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        List<EventResponseDTO> events = eventService.findByState();
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable("eventId") Long eventId,
            @RequestHeader("X-User-Id") String username) {

        eventService.deleteEvent(eventId, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/findBy/organizer/{referenceId}")
    public ResponseEntity<List<EventResponseDTO>> getActiveEventsByOrganizer(
            @PathVariable("referenceId") String referenceId) {

        List<EventResponseDTO> events = eventService.findByStateAndOrganizerId(referenceId);
        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventResponseDTO> cancelEvent(
            @PathVariable("eventId") Long eventId,
            @RequestBody(required = false) CancelEventRequestDTO cancelRequest) {

        if (cancelRequest == null) {
            cancelRequest = new CancelEventRequestDTO();
        }

        EventResponseDTO cancelledEvent = eventService.cancelEvent(eventId, cancelRequest);
        return ResponseEntity.ok(cancelledEvent);
    }
}