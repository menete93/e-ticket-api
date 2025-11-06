package mz.co.mozbuy.e_ticket.event.core.controller;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategoryEntity;
import mz.co.mozbuy.e_ticket.event.core.service.EventCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event-categories")
@RequiredArgsConstructor
public class EventCategoryController {

    private final EventCategoryService service;

    @PostMapping
    public ResponseEntity<EventCategoryEntity> create(@RequestBody EventCategoryEntity category) {
        return ResponseEntity.ok(service.create(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventCategoryEntity> update(@PathVariable Long id, @RequestBody EventCategoryEntity category) {
        category.setId(id);
        return ResponseEntity.ok(service.update(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventCategoryEntity> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EventCategoryEntity>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
