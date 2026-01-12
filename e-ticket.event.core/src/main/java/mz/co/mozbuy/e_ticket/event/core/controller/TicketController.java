package mz.co.mozbuy.e_ticket.event.core.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService eventTicketService;

    @PostMapping
    public ResponseEntity<List<TicketResponseDTO>> createTicket(
            @Valid @RequestBody List<TicketRequestDTO> ticketDTO) {

       List<TicketResponseDTO> createdTicket = eventTicketService.createTickets(ticketDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @Valid @RequestBody TicketRequestDTO ticketDTO) {

        TicketResponseDTO updatedTicket = eventTicketService.updateTicket(ticketDTO.getEventId(), ticketDTO.getTicketId(), ticketDTO);
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
