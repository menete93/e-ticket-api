package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "USER_PAYMENT_METHOD", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_USER_PAYMENT_METHOD_01", columnNames = {"USER_ID", "TOKEN"})
})
@SequenceGenerator(name = "GENERATOR", sequenceName = "USER_PAYMENT_METHOD_SEQ", initialValue = 1, allocationSize = 1)
public class UserPaymentMethodEntity extends AuditableEntity<Long, String> {

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "METHOD_ID")
    private PaymentMethodEntity method;

    @Column(name = "TOKEN", length = 255)
    private String token;

    @Column(name = "LAST_FOUR", length = 4)
    private String lastFour;

    @Column(name = "CARD_BRAND", length = 20)
    private String cardBrand;

    @Column(name = "EXPIRY_MONTH")
    private Integer expiryMonth;

    @Column(name = "EXPIRY_YEAR")
    private Integer expiryYear;

    @Column(name = "IS_DEFAULT")
    private Boolean isDefault = false;

    @Column(name = "PAYER_NAME", length = 100)
    private String payerName;

    @Column(name = "PAYER_EMAIL", length = 100)
    private String payerEmail;

    @Column(name = "PAYER_PHONE", length = 20)
    private String payerPhone;

}