package mz.co.mozbuy.e_ticket.event.auth.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' already exists");
    }

}