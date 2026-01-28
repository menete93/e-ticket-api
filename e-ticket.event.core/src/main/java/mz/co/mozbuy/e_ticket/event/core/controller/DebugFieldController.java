package mz.co.mozbuy.e_ticket.event.core.controller;

import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug-field")
public class DebugFieldController {


    private final EventRepository eventRepository;

    public DebugFieldController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


    @GetMapping("/test-ticket-fields")
    public Map<String, Object> testTicketFields() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Busca eventos com tickets
            List<EventRepository.EventWithTicketsProjection> events = eventRepository.findActiveEventsWithTickets();

            if (!events.isEmpty()) {
                Event event = (Event) events.get(0);
                EventTicket ticket = event.getTickets().get(0);  // Primeiro ticket

                // Teste CADA campo individualmente
                result.put("ticketId", ticket.getId());

                Map<String, String> fieldTests = new LinkedHashMap<>();

                // Teste cada método do mapper original
                testMethod(ticket, "getBenefits", fieldTests);
                testMethod(ticket, "isAvailable", fieldTests);
                testMethod(ticket, "isSalesPeriodActive", fieldTests);
                testMethod(ticket, "getTotalRevenue", fieldTests);
                testMethod(ticket, "getCurrentPrice", fieldTests);
                testMethod(ticket, "getDescription", fieldTests);

                result.put("fieldTests", fieldTests);
            }

            result.put("status", "success");

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    private void testMethod(EventTicket ticket, String methodName, Map<String, String> results) {
        try {
            Object value = null;

            switch (methodName) {
                case "getBenefits":
                    value = ticket.getBenefits();
                    break;
                case "isAvailable":
                    value = ticket.isAvailable();
                    break;
                case "isSalesPeriodActive":
                    value = ticket.isSalesPeriodActive();
                    break;
                case "getTotalRevenue":
                    value = ticket.getTotalRevenue();
                    break;
                case "getCurrentPrice":
                    value = ticket.getCurrentPrice();
                    break;
                case "getDescription":
                    value = ticket.getDescription();
                    break;
            }

            results.put(methodName, "✓ " + (value != null ? value.toString() : "null"));

        } catch (Exception e) {
            results.put(methodName, "✗ " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @GetMapping("/test-benefits-fix")
    public ResponseEntity<?> testBenefitsFix() {
        try {
            // Busque seu evento com tickets
            List<EventRepository.EventWithTicketsProjection> events = eventRepository.findActiveEventsWithTickets();

            if (!events.isEmpty()) {
                Event event = (Event) events.get(0);
                EventTicket ticket = event.getTickets().get(0);

                Map<String, Object> result = new HashMap<>();
                result.put("ticketId", ticket.getId());
                result.put("ticketName", ticket.getTicketName());

                // Agora deve funcionar!
                List<String> benefits = ticket.getBenefits();
                result.put("benefits", benefits);
                result.put("benefitsCount", benefits.size());
                result.put("status", "SUCESSO");

                return ResponseEntity.ok(result);
            }

            return ResponseEntity.ok("Nenhum evento encontrado");

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("ERRO: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}