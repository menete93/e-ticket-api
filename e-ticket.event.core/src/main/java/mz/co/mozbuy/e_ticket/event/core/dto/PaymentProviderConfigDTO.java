package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProviderConfigDTO {

    private Long id;
    private String code;
    private String name;
    private String description;

    // Configurações de API
    private String baseUrl;
    private String apiKey;
    private String apiSecret;
    private String partnerCode;
    private String callbackUrl;
    private String timeoutUrl;

    // Configurações de negócio
    private Integer priority;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal feePercentage;
    private BigDecimal feeFixed;
    private Integer settlementDays;
    private Boolean isInstant;
    private String smsContent;
    private Map<String, Object> extraConfig;

    // Auditoria
    private String state;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Long version;

    public boolean isAvailable() {
        return "ACTIVE".equals(state);
    }
}