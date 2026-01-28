package mz.co.mozbuy.e_ticket.event.core.controller;

import mz.co.mozbuy.e_ticket.event.core.service.JdbcEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jdbc")
public class JdbcEventController {

    @Autowired
    private JdbcEventService jdbcEventService;

    @GetMapping("/events")
    public ResponseEntity<?> getEventsJdbc() {
        try {
            List<Map<String, Object>> events = jdbcEventService.getActiveEventsGrouped();

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "timestamp", LocalDateTime.now(),
                    "count", events.size(),
                    "data", events
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "ERROR",
                            "timestamp", LocalDateTime.now(),
                            "message", "Erro ao buscar eventos via JDBC",
                            "error", e.getMessage(),
                            "errorType", e.getClass().getName()
                    ));
        }
    }

    @GetMapping("/events/raw")
    public ResponseEntity<?> getEventsRaw() {
        try {
            List<Map<String, Object>> rawData = jdbcEventService.getActiveEventsDirect();

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "rowCount", rawData.size(),
                    "data", rawData
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("ERRO raw: " + e.getMessage());
        }
    }
}