package mz.co.mozbuy.common.audit;

import lombok.Getter;

@Getter
public enum LifeCycleState {

    // ✅ ESTADOS BÁSICOS
    ACTIVE("ACTIVE", 0),
    INACTIVE("INACTIVE", 1),
    DELETED("DELETED", 2),
    BLOCKED("BLOCKED", 3),
    BANNED("BANNED", 4),

    // ✅ ESTADOS PARA TICKET_RESERVATION
    CONFIRMED("CONFIRMED", 10),
    EXPIRED("EXPIRED", 11),
    CANCELLED("CANCELLED", 12);

    private final String dbValue;
    private final int code;

    LifeCycleState(String dbValue, int code) {
        this.dbValue = dbValue;
        this.code = code;
    }

    public static LifeCycleState fromDbValue(String dbValue) {
        if (dbValue == null) return ACTIVE;
        String normalized = dbValue.trim().toUpperCase();
        for (LifeCycleState state : values()) {
            if (state.dbValue.equals(normalized)) {
                return state;
            }
        }
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }

    public static LifeCycleState fromCode(int code) {
        for (LifeCycleState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        return ACTIVE;
    }

    @Override
    public String toString() {
        return this.dbValue;
    }
}