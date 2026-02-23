package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class CouponNotValidException extends RuntimeException {

    public CouponNotValidException(String message) {
        super(message);
    }

    public CouponNotValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
