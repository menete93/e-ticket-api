package mz.co.mozbuy.e_ticket.event.core.exceptions;


public class EventAlreadyCancelledException extends RuntimeException {
    public EventAlreadyCancelledException(Long eventId) {
        super("Evento com ID " + eventId + " ja estava cancelado");
    }
}