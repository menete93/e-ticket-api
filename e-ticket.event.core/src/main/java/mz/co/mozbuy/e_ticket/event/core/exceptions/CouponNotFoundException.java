package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class CouponNotFoundException extends RuntimeException{
    public CouponNotFoundException(String message) {
        super(message);
    }
}
