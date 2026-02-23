package mz.co.mozbuy.e_ticket.event.auth.exception;

public class AlreadyOrganizerException extends RuntimeException{

    public AlreadyOrganizerException(Long id) {
        super("user is already organizer"+id);
    }



}
