package mz.co.mozbuy.e_ticket.event.core.enums;

import lombok.Getter;

@Getter
public enum PricingStrategyType {

    EARLY_BIRD("Early Bird", "Desconto para compras antecipadas"),
    LAST_MINUTE("Last Minute", "Aumento na última hora antes do evento"),
    DEMAND_BASED("Demand Based", "Baseado em percentual de vendas/procura"),
    GROUP_DISCOUNT("Group Discount", "Desconto para compras em grupo"),
    CATEGORY_SPECIFIC("Category Specific", "Estratégia específica por categoria de ingresso"),
    FLASH_SALE("Flash Sale", "Promoção relâmpago por tempo limitado"),
    WEEKEND_SPECIAL("Weekend Special", "Preço especial para fins de semana"),
    LOYALTY_DISCOUNT("Loyalty Discount", "Desconto para clientes frequentes"),
    TIERED_PRICING("Tiered Pricing", "Preço por níveis de venda"),
    BUNDLE_DISCOUNT("Bundle Discount", "Desconto para compra de múltiplos ingressos"),
    LOYALTY("Loyalty", "Desconto para clientes frequentes/fiéis"),
    BUNDLE("Bundle", "Desconto para compra de múltiplos ingressos"),
    FIRST_BUYER("First Buyer", "Desconto para primeira compra"),
    VOLUME_BASED("Volume Based", "Desconto baseado em volume de compras"),
    TIME_BASED("Time Based", "Preço baseado no tempo até o evento");

    private String displayName;
    private String description;

    PricingStrategyType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}