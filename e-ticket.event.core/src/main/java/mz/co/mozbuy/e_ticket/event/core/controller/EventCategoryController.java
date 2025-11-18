package mz.co.mozbuy.e_ticket.event.core.controller;



import mz.co.mozbuy.e_ticket.event.core.dto.EventCategoryDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.EventCategoryRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.service.EventCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/event-categories")
@RequiredArgsConstructor
public class EventCategoryController {

    private final EventCategoryService eventCategoryService;

    @GetMapping
    public ResponseEntity<List<EventCategoryDTO>> getAllCategories() {
        List<EventCategoryDTO> categories = eventCategoryService.findAllActive();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventCategoryDTO> getCategoryById(@PathVariable Long id) {
        EventCategoryDTO category = eventCategoryService.findById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<EventCategoryDTO> getCategoryByName(@PathVariable String name) {
        EventCategoryDTO category = eventCategoryService.findByName(name);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventCategoryDTO>> searchCategories(@RequestParam String term) {
        List<EventCategoryDTO> categories = eventCategoryService.search(term);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<EventCategoryDTO>> getPopularCategories() {
        List<EventCategoryDTO> categories = eventCategoryService.findPopularCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<EventCategoryDTO> createCategory(
            @Valid @RequestBody EventCategoryRequestDTO requestDTO,
            @RequestHeader("X-User-Id") String username
    ) {
        EventCategoryDTO createdCategory = eventCategoryService.create(requestDTO, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventCategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody EventCategoryRequestDTO requestDTO) {
        EventCategoryDTO updatedCategory = eventCategoryService.update(id, requestDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
        eventCategoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}