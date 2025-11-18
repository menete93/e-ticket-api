package mz.co.mozbuy.e_ticket.event.core.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.EventTicketRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.EventTicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.service.EventTicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventTicketController {

    private final EventTicketService eventTicketService;

    @PostMapping
    public ResponseEntity<EventTicketResponseDTO> createTicket(
            @Valid @RequestBody EventTicketRequestDTO ticketDTO,
            @RequestHeader("X-User-Id") String username) {

        EventTicketResponseDTO createdTicket = eventTicketService.createTicket(ticketDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<EventTicketResponseDTO> updateTicket(
            @Valid @RequestBody EventTicketRequestDTO ticketDTO) {

        EventTicketResponseDTO updatedTicket = eventTicketService.updateTicket(ticketDTO.getEventId(), ticketDTO.getTicketId(), ticketDTO);
        return ResponseEntity.ok(updatedTicket);
    }

    @PostMapping("/default")
    public ResponseEntity<String> createDefaultTickets(
            @PathVariable Long eventId) {

        // Buscar evento e criar bilhetes padrão
        // eventTicketService.createDefaultTickets(event, capacity);
        return ResponseEntity.ok("Default tickets created successfully");
    }
}
