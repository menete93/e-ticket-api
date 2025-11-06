//package mz.co.mozbuy.e_ticket.event.core.model;
//
//import lombok.Getter;
//
///**
// * Representa o estado de um objecto com ciclo de vida.
// */
//@Getter
//public enum LifeCycleState {
//    ACTIVE(true, 0),
//    INACTIVE(false, 1),
//    DELETED(false, 2),
//    BLOCKED(false, 3),
//    BANNED(false, 4);
//
//    private final boolean active;
//    private final int code;
//
//    LifeCycleState(boolean active, int code) {
//        this.active = active;
//        this.code = code;
//    }
//
//    public static LifeCycleState fromCode(int code) {
//        for (LifeCycleState state : values()) {
//            if (state.code == code) return state;
//        }
//        throw new IllegalArgumentException("Código de estado inválido: " + code);
//    }
//}
//
