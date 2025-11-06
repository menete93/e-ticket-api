package mz.co.mozbuy.e_ticket.event.core.controller;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketCategoryDTO;
import mz.co.mozbuy.e_ticket.event.core.model.TicketCategoryEntity;
import mz.co.mozbuy.e_ticket.event.core.service.TicketCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService service;

    @PostMapping
    public ResponseEntity<TicketCategoryEntity> create(@RequestBody TicketCategoryDTO entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketCategoryEntity> update(@PathVariable Long id, @RequestBody TicketCategoryEntity entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.update(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketCategoryEntity> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TicketCategoryEntity>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<List<TicketCategoryEntity>> findByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(service.findAll().stream()
                .filter(tc -> tc.getEvent().getId().equals(eventId))
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
