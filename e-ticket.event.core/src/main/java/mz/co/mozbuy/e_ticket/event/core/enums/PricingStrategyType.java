package mz.co.mozbuy.e_ticket.event.core.enums;




public enum PricingStrategyType {
    FIXED("FIXED"),
    DYNAMIC("DYNAMIC"),
    TIME_BASED("TIME_BASED"),
    TIERED("TIERED"),
    GROUP_DISCOUNT("GROUP_DISCOUNT"),
    DEMAND_BASED("DEMAND_BASED"),
    PROMOTIONAL("PROMOTIONAL");

    private final String value;

    PricingStrategyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PricingStrategyType fromString(String value) {
        for (PricingStrategyType type : PricingStrategyType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return FIXED; // default
    }
}