package mz.co.mozbuy.common.audit;

import lombok.Getter;

@Getter
public enum LifeCycleState {

    // ✅ VERSÃO CORRETA para @Enumerated(EnumType.STRING)
    ACTIVE("ACTIVE", 0),
    INACTIVE("INACTIVE", 1),
    DELETED("DELETED", 2),
    BLOCKED("BLOCKED", 3),
    BANNED("BANNED", 4);

    private final String dbValue;  // Valor no banco (string)
    private final int code;        // Código numérico para lógica interna

    LifeCycleState(String dbValue, int code) {
        this.dbValue = dbValue;
        this.code = code;
    }

    // Converte do banco para enum
    public static LifeCycleState fromDbValue(String dbValue) {
        if (dbValue == null) return ACTIVE;

        String normalized = dbValue.trim().toUpperCase();
        for (LifeCycleState state : values()) {
            if (state.dbValue.equals(normalized)) {
                return state;
            }
        }

        // Fallback: tenta pelo nome do enum
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }

    // Converte código para enum (mantém compatibilidade)
    public static LifeCycleState fromCode(int code) {
        for (LifeCycleState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        return ACTIVE;
    }

    // Para JPQL/String comparação
    @Override
    public String toString() {
        return this.dbValue;
    }
}