package mz.co.mozbuy.e_ticket.event.auth.exception;

public class NotAnOrganizerException extends RuntimeException {
    public NotAnOrganizerException(Long id) {
        super("not an organizer"+id);
    }
}
