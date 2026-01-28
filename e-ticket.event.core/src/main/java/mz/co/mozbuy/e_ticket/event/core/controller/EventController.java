// EventController.java
package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.EventRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.EventResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import mz.co.mozbuy.e_ticket.event.core.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventRepository eventRepository;

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO eventDTO) {

        EventResponseDTO createdEvent = eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent(@PathVariable Long eventId) {
        EventResponseDTO event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestDTO eventDTO
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
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") String username) {

        eventService.deleteEvent(eventId, username);
        return ResponseEntity.noContent().build();
    }


//    @GetMapping("/test-jpql")
//    public ResponseEntity<String> testJPQL() {
//        try {
//            List<Event> events = eventRepository.findActiveEventsWithTickets();
//
//            if (!events.isEmpty()) {
//                Event primeiro = events.get(0);
//                return ResponseEntity.ok("Sucesso! " + events.size() + " eventos. " +
//                        "Primeiro evento tem " + primeiro.getTickets().size() + " tickets");
//            }
//            return ResponseEntity.ok("Sucesso! " + events.size() + " eventos");
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                    .body("Erro: " + e.getMessage());
//        }
//    }



}