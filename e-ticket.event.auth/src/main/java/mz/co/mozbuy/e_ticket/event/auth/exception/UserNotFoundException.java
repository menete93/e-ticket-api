package mz.co.mozbuy.e_ticket.event.auth.exception;

import mz.co.mozbuy.e_ticket.event.auth.dto.UserContext;

public class UserNotFoundException extends RuntimeException {
    public  UserNotFoundException(String username) {
        super("User not found"+username);
    }

    public  UserNotFoundException(Long id) {
        super("User not found"+id);
    }
}
