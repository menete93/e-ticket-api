package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class OrganizerNotAllowedException extends RuntimeException {
    public OrganizerNotAllowedException(String message,String organizerName,String lifeCycleState) {
        super();
    }

}