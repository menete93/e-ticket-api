package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Long Id) {
        super("Ticket with id " + Id + " not found");
    }
}
