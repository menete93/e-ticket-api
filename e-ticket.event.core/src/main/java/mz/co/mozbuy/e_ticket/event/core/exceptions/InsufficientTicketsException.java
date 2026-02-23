package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class InsufficientTicketsException extends RuntimeException{
    public InsufficientTicketsException() {
        super("Insufficient tickets for this event: " );
    }
}
