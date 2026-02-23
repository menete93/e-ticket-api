package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.OrganizerRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.OrganizerResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.OrganizerStatsDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Organizer;
import mz.co.mozbuy.e_ticket.event.core.service.OrganizerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizers")
@RequiredArgsConstructor
public class OrganizerController {

    private final OrganizerService organizerService;

    @PostMapping
    public ResponseEntity<OrganizerResponseDTO> createOrganizer(
            @Valid @RequestBody OrganizerRequestDTO requestDTO) {
        System.out.println("EMAIL RECEBIDO => [" + requestDTO.getEmail() + "]");

        OrganizerResponseDTO organizer = organizerService.createOrganizer(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(organizer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizerResponseDTO> getOrganizer(@PathVariable Long id) {
        OrganizerResponseDTO organizer = organizerService.getOrganizerById(id);
        return ResponseEntity.ok(organizer);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<OrganizerResponseDTO> getOrganizerByEmail(@PathVariable String email) {
        OrganizerResponseDTO organizer = organizerService.getOrganizerByEmail(email);
        return ResponseEntity.ok(organizer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizerResponseDTO> updateOrganizer(
            @PathVariable Long id,
            @Valid @RequestBody OrganizerRequestDTO requestDTO) {
        OrganizerResponseDTO organizer = organizerService.updateOrganizer(id, requestDTO);
        return ResponseEntity.ok(organizer);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<OrganizerStatsDTO> getOrganizerStats(@PathVariable Long id) {
        OrganizerStatsDTO stats = organizerService.getOrganizerStats(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/active")
    public ResponseEntity<List<OrganizerResponseDTO>> getActiveOrganizers() {
        List<OrganizerResponseDTO> organizers = organizerService.getActiveOrganizers();
        return ResponseEntity.ok(organizers);
    }

    @GetMapping("/top-earners")
    public ResponseEntity<List<OrganizerResponseDTO>> getTopEarners(
            @RequestParam(defaultValue = "1000") BigDecimal minEarnings) {
        List<OrganizerResponseDTO> organizers = organizerService.getTopEarners(minEarnings);
        return ResponseEntity.ok(organizers);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateOrganizer(@PathVariable Long id) {
        organizerService.deactivateOrganizer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateOrganizer(@PathVariable Long id) {
        organizerService.activateOrganizer(id);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping
//    public ResponseEntity<OrganizerResponseDTO> createOrganizerProfile(
//            @Valid @RequestBody OrganizerProfileRequest request) {
//
//        Organizer profile = organizerService.createProfile(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(profile));
//    }
}