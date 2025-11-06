package mz.co.mozbuy.e_ticket.event.core.controller;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.SalePhaseEntity;
import mz.co.mozbuy.e_ticket.event.core.service.SalePhaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sale-phases")
@RequiredArgsConstructor
public class SalePhaseController {

    private final SalePhaseService service;

    @PostMapping
    public ResponseEntity<SalePhaseEntity> create(@RequestBody SalePhaseEntity entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalePhaseEntity> update(@PathVariable Long id, @RequestBody SalePhaseEntity entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.update(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalePhaseEntity> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<SalePhaseEntity>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<List<SalePhaseEntity>> findByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(service.findAll().stream()
                .filter(sp -> sp.getEvent().getId().equals(eventId))
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
