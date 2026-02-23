package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class SalesPeriodNotActiveException extends RuntimeException{
    public SalesPeriodNotActiveException() {
        super( "SalesPeriod Not Active" );
    }
}
