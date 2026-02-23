package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class OrganizerNotFoundException extends RuntimeException {

    public OrganizerNotFoundException(Long organizerId) {
        super("Organizer não encontrado com ID: " + organizerId);
    }
}