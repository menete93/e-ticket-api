//package mz.co.mozbuy.e_ticket.event.core.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import mz.co.mozbuy.common.audit.AuditableEntity;
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.type.SqlTypes;
//
//import java.util.Map;
//
//@Entity
//@Table(name = "payment_failure")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class PaymentFailureEntity extends AuditableEntity<Long, String> {
//
//    @Column(name = "payment_transaction_id", nullable = false, unique = true)
//    private Long paymentTransactionId;
//
//    @Column(name = "attempt_number", nullable = false)
//    private Integer attemptNumber;
//
//    @Column(name = "provider_result_code", length = 50)
//    private String providerResultCode;
//
//    @Column(name = "provider_result_desc", length = 500)
//    private String providerResultDesc;
//
//    @Column(name = "http_status_code")
//    private Integer httpStatusCode;
//
//    @Column(name = "failure_category", length = 50)
//    private String failureCategory;
//
//    @Column(name = "failure_code", length = 50)
//    private String failureCode;
//
//    @Column(name = "failure_message", length = 500)
//    private String failureMessage;
//
//    @Column(name = "failure_details", columnDefinition = "TEXT")
//    private String failureDetails;
//
//    @Column(name = "can_retry")
//    private Boolean canRetry;
//
//    @Column(name = "retry_recommended")
//    private Boolean retryRecommended;
//
//    @Column(name = "resolved")
//    private Boolean resolved;
//
//    @Column(name = "resolution_action", length = 50)
//    private String resolutionAction;
//
//    @Column(name = "resolved_by", length = 100)
//    private String resolvedBy;
//
//    @Column(name = "resolution_notes", columnDefinition = "TEXT")
//    private String resolutionNotes;
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "request_payload", columnDefinition = "jsonb")
//    private Map<String, Object> requestPayload;
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "response_payload", columnDefinition = "jsonb")
//    private Map<String, Object> responsePayload;
//
//    @PrePersist
//    protected void onCreate() {
//        if (canRetry == null) canRetry = true;
//        if (retryRecommended == null) retryRecommended = true;
//        if (resolved == null) resolved = false;
//        if (attemptNumber == null) attemptNumber = 1;
//    }
//}