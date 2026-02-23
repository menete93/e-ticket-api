package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class TicketCreationException extends RuntimeException {
    public TicketCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}