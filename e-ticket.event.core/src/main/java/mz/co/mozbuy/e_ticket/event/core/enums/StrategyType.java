package mz.co.mozbuy.e_ticket.event.core.enums;

public enum StrategyType {
    MANUAL, SCHEDULED, DYNAMIC, AUTO;

    public String valueOf() {
        return this.name();
    }
}
