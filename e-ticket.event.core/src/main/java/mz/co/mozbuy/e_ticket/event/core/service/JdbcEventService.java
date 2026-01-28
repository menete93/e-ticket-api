package mz.co.mozbuy.e_ticket.event.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JdbcEventService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getActiveEventsDirect() {
        String sql = """
            SELECT 
                e.id as event_id,
                e.name as event_name,
                e.description as event_description,
                e.life_cycle_state as event_state,
                e.event_date as event_date,
                t.id as ticket_id,
                t.ticket_name as ticket_name,
                t.current_price as ticket_price,
                t.available_quantity as ticket_quantity,
                t.is_active as ticket_active
            FROM e_ticket.events e
            LEFT JOIN e_ticket.event_tickets t ON e.id = t.event_id
            WHERE e.life_cycle_state = 'ACTIVE'
            ORDER BY e.id, t.id
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();

            // Evento
            row.put("eventId", rs.getLong("event_id"));
            row.put("eventName", rs.getString("event_name"));
            row.put("eventDescription", rs.getString("event_description"));
            row.put("eventState", rs.getString("event_state"));
            row.put("eventDate", rs.getTimestamp("event_date"));

            // Ticket (pode ser null no LEFT JOIN)
            if (rs.getObject("ticket_id") != null) {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("id", rs.getLong("ticket_id"));
                ticket.put("name", rs.getString("ticket_name"));
                ticket.put("price", rs.getBigDecimal("ticket_price"));
                ticket.put("quantity", rs.getInt("ticket_quantity"));
                ticket.put("active", rs.getBoolean("ticket_active"));
                row.put("ticket", ticket);
            } else {
                row.put("ticket", null);
            }

            return row;
        });
    }

    public List<Map<String, Object>> getActiveEventsGrouped() {
        List<Map<String, Object>> rawData = getActiveEventsDirect();

        // Agrupa tickets por evento
        Map<Long, Map<String, Object>> eventsMap = new LinkedHashMap<>();

        for (Map<String, Object> row : rawData) {
            Long eventId = (Long) row.get("eventId");

            if (!eventsMap.containsKey(eventId)) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", eventId);
                event.put("name", row.get("eventName"));
                event.put("description", row.get("eventDescription"));
                event.put("state", row.get("eventState"));
                event.put("date", row.get("eventDate"));
                event.put("tickets", new ArrayList<Map<String, Object>>());
                eventsMap.put(eventId, event);
            }

            // Adiciona ticket se existir
            if (row.get("ticket") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> tickets =
                        (List<Map<String, Object>>) eventsMap.get(eventId).get("tickets");
                tickets.add((Map<String, Object>) row.get("ticket"));
            }
        }

        return new ArrayList<>(eventsMap.values());
    }
}