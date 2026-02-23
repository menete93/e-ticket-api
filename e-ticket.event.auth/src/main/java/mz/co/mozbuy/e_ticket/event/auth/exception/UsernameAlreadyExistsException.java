package mz.co.mozbuy.e_ticket.event.auth.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Username '" + username + "' already exists");
    }
}