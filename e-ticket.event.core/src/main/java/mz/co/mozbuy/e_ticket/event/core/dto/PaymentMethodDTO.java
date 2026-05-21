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
public class PaymentMethodDTO {

    private Long id;
    private String code;
    private String name;
    private String iconUrl;
    private Integer displayOrder;
    private PaymentProviderConfigDTO provider;
    private Long providerId;

    private Boolean requiresPhone;
    private Boolean requiresCard;
    private Boolean requiresQrCode;
    private Boolean requiresDocument;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Map<String, Object> displayConfig;

    // Auditoria
    private String state;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Long version;

    public boolean isAvailable() {
        return "ACTIVE".equals(state) && provider != null && provider.isAvailable();
    }
}
