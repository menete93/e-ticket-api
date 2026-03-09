package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class StrategyCreationException extends RuntimeException {
    public StrategyCreationException(String message,Exception e) {
        super(message);
    }
}
