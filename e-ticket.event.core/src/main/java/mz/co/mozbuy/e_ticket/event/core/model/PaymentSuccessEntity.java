//package mz.co.mozbuy.e_ticket.event.core.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import mz.co.mozbuy.common.audit.AuditableEntity;
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.type.SqlTypes;
//
//import java.math.BigDecimal;
//import java.util.Map;
//
//@Entity
//@Table(name = "payment_success")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class PaymentSuccessEntity extends AuditableEntity<Long, String> {
//
//
//    @Column(name = "payment_transaction_id", nullable = false, unique = true)
//    private Long paymentTransactionId;
//
//    @Column(name = "provider_transaction_id", length = 100)
//    private String providerTransactionId;
//
//    @Column(name = "provider_result_code", length = 50)
//    private String providerResultCode;
//
//    @Column(name = "provider_result_desc", length = 500)
//    private String providerResultDesc;
//
//    @Column(name = "confirmed_amount", nullable = false, precision = 15, scale = 2)
//    private BigDecimal confirmedAmount;
//
//    @Column(name = "fee_amount", precision = 15, scale = 2)
//    private BigDecimal feeAmount;
//
//    @Column(name = "net_amount", precision = 15, scale = 2)
//    private BigDecimal netAmount;
//
//    @Column(name = "currency", length = 3)
//    private String currency;
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "request_payload", columnDefinition = "jsonb")
//    private Map<String, Object> requestPayload;
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "response_payload", columnDefinition = "jsonb")
//    private Map<String, Object> responsePayload;
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "callback_payload", columnDefinition = "jsonb")
//    private Map<String, Object> callbackPayload;
//
//    @Column(name = "receipt_url", length = 500)
//    private String receiptUrl;
//}