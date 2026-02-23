package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(Long id) {
        super("Event not found with id: " + id);
    }


    public EventNotFoundException() {
        super("No active events found ");
    }
}
