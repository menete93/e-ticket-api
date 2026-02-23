package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class TicketNotActiveException extends RuntimeException{
    public TicketNotActiveException() {
        super( "Ticket Not Active" );
    }

}
