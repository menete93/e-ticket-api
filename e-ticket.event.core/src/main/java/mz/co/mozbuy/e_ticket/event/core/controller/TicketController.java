package mz.co.mozbuy.e_ticket.event.core.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.BatchUpdateTicketsDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.UpdateTicketQuantityDTO;
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


    @PutMapping("/event/{eventId}/tickets/batch")
    public ResponseEntity<List<TicketResponseDTO>> batchUpdateTickets(
            @PathVariable("eventId") Long eventId,
            @RequestBody BatchUpdateTicketsDTO batchUpdate) {

        if (!eventId.equals(batchUpdate.getEventId())) {
            throw new IllegalArgumentException("Event ID in path does not match request body");
        }

        List<TicketResponseDTO> updated = eventTicketService.batchUpdateTickets(batchUpdate);
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/default")
    public ResponseEntity<String> createDefaultTickets(
            @PathVariable("default") Long eventId) {

        // Buscar evento e criar bilhetes padrão
        // eventTicketService.createDefaultTickets(event, capacity);
        return ResponseEntity.ok("Default tickets created successfully");
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<List<TicketResponseDTO>> getTicket(
            @Valid @PathVariable("eventId") Long eventId) {

      List<TicketResponseDTO>   tickets = eventTicketService.getTicketsByEventId(eventId);
        return ResponseEntity.ok(tickets);
    }

}
