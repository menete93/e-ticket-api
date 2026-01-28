package mz.co.mozbuy.e_ticket.event.core.controller;

import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.EventSimpleDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.EventWithTicketsDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;

import mz.co.mozbuy.e_ticket.event.core.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
public class DiagnosticController {

    @Autowired
    private EventService eventService;

    private  EventRepository eventRepository;


    @GetMapping("/active")
    public ResponseEntity<?> getActiveEvents() {
        try {
            List<EventWithTicketsDTO> events = eventService.getActiveEventsWithTickets();

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "timestamp", LocalDateTime.now(),
                    "count", events.size(),
                    "data", events
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "ERROR",
                            "timestamp", LocalDateTime.now(),
                            "message", "Erro ao buscar eventos",
                            "error", e.getMessage()
                    ));
        }
    }


    @GetMapping("/test-simple")
    public ResponseEntity<?> testSimple() {
        try {
            List<EventSimpleDTO> events = eventRepository.findActiveEventsSimple();
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "count", events.size(),
                    "events", events
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("ERRO simples: " + e.getMessage());
        }
    }
}
