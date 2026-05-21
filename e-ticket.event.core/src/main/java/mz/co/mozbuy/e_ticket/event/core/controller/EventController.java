// EventController.java
package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.*;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import mz.co.mozbuy.e_ticket.event.core.service.EventService;
import mz.co.mozbuy.e_ticket.event.core.service.task.EventLifeCycleService;
import mz.co.mozbuy.e_ticket.event.core.mapper.EventMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final EventLifeCycleService eventLifeCycleService;  // ← ADICIONAR
    private final EventMapper eventMapper;  // ← ADICIONAR

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO eventDTO) {

        EventResponseDTO createdEvent = eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent(@PathVariable("eventId") Long eventId) {
        EventResponseDTO event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody EventUpdateDTO eventDTO) {

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


    /**
     * Buscar TODOS os eventos do organizador (sem filtro de estado)
     */
    @GetMapping("/findAll/organizer/{referenceId}")
    public ResponseEntity<List<EventResponseDTO>> getAllEventsByOrganizer(
            @PathVariable("referenceId") String referenceId) {

        List<EventResponseDTO> events = eventService.getAllEventsByOrganizer(referenceId);
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

    // ==================== ENDPOINTS DE ESTADO ====================

    /**
     * Buscar eventos por estado (ACTIVE, INACTIVE, BANNED, etc.)
     */
    @GetMapping("/by-state/{state}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByState(@PathVariable("state") String state) {
        log.info("🔍 Buscando eventos com estado: {}", state);

        LifeCycleState lifeCycleState = LifeCycleState.fromDbValue(state);
        List<Event> events = eventRepository.findByState(lifeCycleState);
        List<EventResponseDTO> response = events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());

        log.info("✅ Encontrados {} eventos com estado {}", response.size(), state);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualizar estado de um evento específico (manual)
     */
    @PostMapping("/{eventId}/update-state")
    public ResponseEntity<Map<String, Object>> updateEventState(@PathVariable("eventId") Long eventId) {
        log.info("🔄 Atualizando estado manual do evento ID: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + eventId));

        LifeCycleState oldState = event.getState();
        LifeCycleState newState = eventLifeCycleService.updateEventState(eventId);

        Map<String, Object> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("eventName", event.getName());
        response.put("oldState", oldState);
        response.put("newState", newState);
        response.put("message", String.format("Estado do evento atualizado de %s para %s", oldState, newState));
        response.put("timestamp", LocalDateTime.now());

        log.info("✅ Evento {} atualizado: {} -> {}", eventId, oldState, newState);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualizar estado de todos os eventos (forçar execução da task)
     */
    @PostMapping("/update-all-states")
    public ResponseEntity<Map<String, Object>> updateAllEventsState() {
        log.info("🔄 Forçando atualização de estado de todos os eventos");

        LocalDateTime startTime = LocalDateTime.now();
        eventLifeCycleService.updateEventLifeCycleStates();
        LocalDateTime endTime = LocalDateTime.now();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Todos os eventos foram atualizados");
        response.put("timestamp", startTime);
        response.put("executionTimeMs", java.time.Duration.between(startTime, endTime).toMillis());

        log.info("✅ Atualização concluída em {} ms",
                java.time.Duration.between(startTime, endTime).toMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Buscar eventos cancelados
     */
    @GetMapping("/cancelled")
    public ResponseEntity<List<EventResponseDTO>> getCancelledEvents() {
        log.info("🔍 Buscando eventos cancelados");

        List<Event> events = eventRepository.findByIsCancelledTrue();
        List<EventResponseDTO> response = events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());

        log.info("✅ Encontrados {} eventos cancelados", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar eventos expirados (data passada mas não cancelados)
     */
    @GetMapping("/expired")
    public ResponseEntity<List<EventResponseDTO>> getExpiredEvents() {
        log.info("🔍 Buscando eventos expirados");

        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByEventDateBeforeAndIsCancelledFalse(now);
        List<EventResponseDTO> response = events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());

        log.info("✅ Encontrados {} eventos expirados", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar eventos ativos (não cancelados e data futura)
     */
    @GetMapping("/active")
    public ResponseEntity<List<EventResponseDTO>> getActiveEvents() {
        log.info("🔍 Buscando eventos ativos");

        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByIsCancelledFalseAndEventDateAfter(now);
        List<EventResponseDTO> response = events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());

        log.info("✅ Encontrados {} eventos ativos", response.size());
        return ResponseEntity.ok(response);
    }



}