package mz.co.mozbuy.e_ticket.event.core.controller;
import mz.co.mozbuy.e_ticket.event.core.dto.EventDto;
import mz.co.mozbuy.e_ticket.event.core.dto.EventRequestDTO;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.EventEntity;
import mz.co.mozbuy.e_ticket.event.core.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventEntity> create(@RequestBody EventRequestDTO event) {
        return ResponseEntity.ok(eventService.create(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventEntity> update(@PathVariable Long id, @RequestBody EventEntity event) {
        event.setId(id);
        return ResponseEntity.ok(eventService.update(event));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventEntity> findById(@PathVariable Long id) {
        return eventService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> findAll() {
        return ResponseEntity.ok(eventService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/by-organizer/{organizerId}")
//    public ResponseEntity<List<EventEntity>> findByOrganizer(@PathVariable Long organizerId) {
//        return ResponseEntity.ok(eventService.findAll().stream()
//                .filter(e -> e.getOrganizer().getId().equals(organizerId)).toList());
//    }
}
