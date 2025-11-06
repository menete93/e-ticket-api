package mz.co.mozbuy.common.audit;


import lombok.Getter;

@Getter
public enum LifeCycleState {

    ACTIVE(0),
    INACTIVE(1),
    DELETED(2),
    BLOCKED(3),
    BANNED(4);

    private final int code;

    LifeCycleState(int code) {
        this.code = code;
    }

    public static LifeCycleState fromCode(int code) {
        return switch (code) {
            case 1 -> INACTIVE;
            case 2 -> DELETED;
            case 3 -> BLOCKED;
            case 4 -> BANNED;
            default -> ACTIVE;
        };
    }
}
