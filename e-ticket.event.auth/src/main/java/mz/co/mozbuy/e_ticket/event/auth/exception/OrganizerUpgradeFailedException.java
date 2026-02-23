package mz.co.mozbuy.e_ticket.event.auth.exception;

import feign.FeignException;

public class OrganizerUpgradeFailedException extends RuntimeException {
    public OrganizerUpgradeFailedException(Long id,String message) {
        super("Organizer upgrade failed"+id +  message );
    }

    public OrganizerUpgradeFailedException(String s, FeignException e) {
        super(s, e);
    }

    public OrganizerUpgradeFailedException(String operationInterrupted, InterruptedException ie) {
        super("Operation interrupted"+operationInterrupted+ie);
    }

    public OrganizerUpgradeFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
