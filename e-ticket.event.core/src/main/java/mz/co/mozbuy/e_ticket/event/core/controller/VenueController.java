package mz.co.mozbuy.e_ticket.event.core.controller;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.VenueEntity;
import mz.co.mozbuy.e_ticket.event.core.service.VenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueEntity> create(@RequestBody VenueEntity venue) {
        return ResponseEntity.ok(venueService.create(venue));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueEntity> update(@PathVariable Long id, @RequestBody VenueEntity venue) {
        venue.setId(id);
        return ResponseEntity.ok(venueService.update(venue));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueEntity> findById(@PathVariable Long id) {
        return venueService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<VenueEntity>> findAll() {
        return ResponseEntity.ok(venueService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
