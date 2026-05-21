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
@Table(name = "PAYMENT_METHOD", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_PAYMENT_METHOD_01", columnNames = {"CODE"})
})
@SequenceGenerator(name = "GENERATOR", sequenceName = "PAYMENT_METHOD_SEQ", initialValue = 1, allocationSize = 1)
public class PaymentMethodEntity extends AuditableEntity<Long, String> {

    @Column(name = "CODE", nullable = false, length = 30)
    private String code;  // MPESA, EMOLA, CARD, BANK_TRANSFER

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "ICON_URL", length = 255)
    private String iconUrl;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder = 0;

    @ManyToOne
    @JoinColumn(name = "PROVIDER_ID")
    private PaymentProviderConfigEntity provider;

    @Column(name = "REQUIRES_PHONE")
    private Boolean requiresPhone = false;

    @Column(name = "REQUIRES_CARD")
    private Boolean requiresCard = false;

    @Column(name = "REQUIRES_QR_CODE")
    private Boolean requiresQrCode = false;

    @Column(name = "REQUIRES_DOCUMENT")
    private Boolean requiresDocument = false;

    @Column(name = "MIN_AMOUNT", precision = 10, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "MAX_AMOUNT", precision = 10, scale = 2)
    private BigDecimal maxAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "DISPLAY_CONFIG", columnDefinition = "json")
    private Map<String, Object> displayConfig;
}