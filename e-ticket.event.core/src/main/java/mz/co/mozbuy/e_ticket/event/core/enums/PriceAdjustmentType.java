package mz.co.mozbuy.e_ticket.event.core.enums;

public enum PriceAdjustmentType {
    PERCENTAGE_INCREASE,    // Aumento percentual
    PERCENTAGE_DECREASE,    // Redução percentual
    FIXED_INCREASE,         // Aumento fixo
    FIXED_DECREASE,         // Redução fixa
    SET_PRICE               // Definir preço específico
}