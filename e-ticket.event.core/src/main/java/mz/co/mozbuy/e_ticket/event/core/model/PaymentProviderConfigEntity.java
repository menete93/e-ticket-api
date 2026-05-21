package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
        import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "PAYMENT_PROVIDER_CONFIG", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_PAYMENT_PROVIDER_CONFIG_01", columnNames = {"CODE"}),
        @UniqueConstraint(name = "UQ_PAYMENT_PROVIDER_CONFIG_02", columnNames = {"PARTNER_CODE"})
})
@SequenceGenerator(name = "GENERATOR", sequenceName = "PAYMENT_PROVIDER_CONFIG_SEQ", initialValue = 1, allocationSize = 1)
public class PaymentProviderConfigEntity extends AuditableEntity<Long, String> {

    @Column(name = "CODE", nullable = false, length = 30)
    private String code;  // MPESA, EMOLA, VISA, etc.

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    // Configurações de API
    @Column(name = "BASE_URL", length = 255)
    private String baseUrl;

    @Column(name = "API_KEY", length = 255)
    private String apiKey;

    @Column(name = "PORT", length = 10)
    private String port;

    @Column(name = "PATH", length = 255)  // ← ADICIONADO
    private String path;

    @Column(name = "API_SECRET", columnDefinition = "TEXT")
    private String apiSecret;

    @Column(name = "PARTNER_CODE", length = 50)
    private String partnerCode;

    @Column(name = "CALLBACK_URL", length = 255)
    private String callbackUrl;

    @Column(name = "TIMEOUT_URL", length = 255)
    private String timeoutUrl;

    // Configurações de negócio
    @Column(name = "PRIORITY")
    private Integer priority = 0;

    @Column(name = "MIN_AMOUNT", precision = 10, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "MAX_AMOUNT", precision = 10, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "FEE_PERCENTAGE", precision = 5, scale = 2)
    private BigDecimal feePercentage = BigDecimal.ZERO;

    @Column(name = "FEE_FIXED", precision = 10, scale = 2)
    private BigDecimal feeFixed = BigDecimal.ZERO;

    @Column(name = "SETTLEMENT_DAYS")
    private Integer settlementDays = 0;

    @Column(name = "IS_INSTANT")
    private Boolean isInstant = false;

    @Column(name = "SMSCONTENT", columnDefinition = "TEXT")
    private String smsContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "EXTRA_CONFIG", columnDefinition = "json")
    private Map<String, Object> extraConfig;


}