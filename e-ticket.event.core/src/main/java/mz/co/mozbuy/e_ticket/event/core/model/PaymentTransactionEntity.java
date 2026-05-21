package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "PAYMENT_TRANSACTION", indexes = {
        @Index(name = "IDX_PAYMENT_TRANSACTION_01", columnList = "TRANSACTION_ID"),
        @Index(name = "IDX_PAYMENT_TRANSACTION_02", columnList = "SALE_ID"),
        @Index(name = "IDX_PAYMENT_TRANSACTION_03", columnList = "STATUS"),
        @Index(name = "IDX_PAYMENT_TRANSACTION_04", columnList = "PROVIDER_CHECKOUT_ID"),
        @Index(name = "IDX_PAYMENT_TRANSACTION_05", columnList = "CREATED_AT")
})
public class PaymentTransactionEntity extends AuditableEntity<Long, String> {

    // ==================== IDENTIFICADORES ====================

    @Column(name = "TRANSACTION_ID", nullable = false, unique = true, length = 50)
    private String transactionId;

    @Column(name = "EXTERNAL_ID", length = 100)
    private String externalId;

    @Column(name = "SALE_ID", nullable = false)
    private Long saleId;

    @Column(name = "EVENT_ID", nullable = false)
    private Long eventId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "ORDER_ID", length = 50)
    private String orderId;

    // ==================== RELACIONAMENTOS ====================

    // 🔥 Relacionamento com a venda (TicketSale)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SALE_ID", insertable = false, updatable = false)
    private TicketSale sale;

    // 🔥 Relacionamento com o evento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID", insertable = false, updatable = false)
    private Event event;

    // 🔥 Relacionamento com o ticket (se aplicável)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKET_ID", insertable = false, updatable = false)
    private EventTicket ticket;

    // 🔥 Relacionamento com método de pagamento
    @ManyToOne
    @JoinColumn(name = "METHOD_ID")
    private PaymentMethodEntity method;

    // 🔥 Relacionamento com provedor
    @ManyToOne
    @JoinColumn(name = "PROVIDER_ID")
    private PaymentProviderConfigEntity provider;

    // ==================== VALORES ====================

    @Column(name = "AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "FEE_AMOUNT", precision = 10, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "DISCOUNT_AMOUNT", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "NET_AMOUNT", precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "CURRENCY", length = 3)
    private String currency;

    // ==================== DADOS DO CLIENTE ====================

    @Column(name = "PAYER_NAME", length = 100)
    private String payerName;

    @Column(name = "PAYER_EMAIL", length = 100)
    private String payerEmail;

    @Column(name = "PAYER_PHONE", length = 20)
    private String payerPhone;

    @Column(name = "PAYER_DOCUMENT", length = 50)
    private String payerDocument;

    // ==================== STATUS ====================

    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

    @Column(name = "STATUS_CODE")
    private Integer statusCode;

    @Column(name = "STATUS_MESSAGE", length = 255)
    private String statusMessage;

    // ==================== DADOS DO PROVEDOR ====================

    @Column(name = "PROVIDER_CHECKOUT_ID", length = 100)
    private String providerCheckoutId;

    @Column(name = "PROVIDER_MERCHANT_ID", length = 100)
    private String providerMerchantId;

    @Column(name = "PROVIDER_TRANSACTION_ID", length = 100)
    private String providerTransactionId;

    @Column(name = "PROVIDER_RESULT_CODE")
    private Integer providerResultCode;

    @Column(name = "PROVIDER_RESULT_DESC", length = 255)
    private String providerResultDesc;

    // ==================== PAYLOADS ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "REQUEST_PAYLOAD", columnDefinition = "json")
    private Map<String, Object> requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "RESPONSE_PAYLOAD", columnDefinition = "json")
    private Map<String, Object> responsePayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "CALLBACK_PAYLOAD", columnDefinition = "json")
    private Map<String, Object> callbackPayload;

    // ==================== CONTROLE ====================

    @Column(name = "RETRY_COUNT")
    private Integer retryCount;

    @Column(name = "MAX_RETRIES")
    private Integer maxRetries;

    @Column(name = "PROCESSED_AT")
    private LocalDateTime processedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @Column(name = "EXPIRES_AT")
    private LocalDateTime expiresAt;

    // ==================== MÉTODOS AUXILIARES ====================

    public String getEventName() {
        return event != null ? event.getName() : null;
    }

    public String getTicketName() {
        return ticket != null ? ticket.getTicketName() : null;
    }

    public Integer getQuantity() {
        return sale != null ? sale.getQuantity() : null;
    }

    public BigDecimal getUnitPrice() {
        return ticket != null ? ticket.getCurrentPrice() : null;
    }

    // ==================== CONSTRUTORES ====================

    public PaymentTransactionEntity() {
        this.feeAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.currency = "MZN";
        this.retryCount = 0;
        this.maxRetries = 3;
    }

    private PaymentTransactionEntity(Builder builder) {
        this();
        this.transactionId = builder.transactionId;
        this.externalId = builder.externalId;
        this.saleId = builder.saleId;
        this.eventId = builder.eventId;
        this.userId = builder.userId;
        this.orderId = builder.orderId;
        this.method = builder.method;
        this.provider = builder.provider;
        this.amount = builder.amount;
        this.feeAmount = builder.feeAmount != null ? builder.feeAmount : BigDecimal.ZERO;
        this.discountAmount = builder.discountAmount != null ? builder.discountAmount : BigDecimal.ZERO;
        this.currency = builder.currency != null ? builder.currency : "MZN";
        this.payerName = builder.payerName;
        this.payerEmail = builder.payerEmail;
        this.payerPhone = builder.payerPhone;
        this.payerDocument = builder.payerDocument;
        this.status = builder.status;
        this.statusCode = builder.statusCode;
        this.statusMessage = builder.statusMessage;
        this.providerCheckoutId = builder.providerCheckoutId;
        this.providerMerchantId = builder.providerMerchantId;
        this.providerTransactionId = builder.providerTransactionId;
        this.providerResultCode = builder.providerResultCode;
        this.providerResultDesc = builder.providerResultDesc;
        this.requestPayload = builder.requestPayload;
        this.responsePayload = builder.responsePayload;
        this.callbackPayload = builder.callbackPayload;
        this.retryCount = builder.retryCount != null ? builder.retryCount : 0;
        this.maxRetries = builder.maxRetries != null ? builder.maxRetries : 3;
        this.processedAt = builder.processedAt;
        this.completedAt = builder.completedAt;
        this.expiresAt = builder.expiresAt;
        calculateNetAmount();
    }

    // ==================== GETTERS E SETTERS ====================

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public Long getSaleId() { return saleId; }
    public void setSaleId(Long saleId) { this.saleId = saleId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public PaymentMethodEntity getMethod() { return method; }
    public void setMethod(PaymentMethodEntity method) { this.method = method; }

    public PaymentProviderConfigEntity getProvider() { return provider; }
    public void setProvider(PaymentProviderConfigEntity provider) { this.provider = provider; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; calculateNetAmount(); }

    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; calculateNetAmount(); }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; calculateNetAmount(); }

    public BigDecimal getNetAmount() { return netAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }

    public String getPayerEmail() { return payerEmail; }
    public void setPayerEmail(String payerEmail) { this.payerEmail = payerEmail; }

    public String getPayerPhone() { return payerPhone; }
    public void setPayerPhone(String payerPhone) { this.payerPhone = payerPhone; }

    public String getPayerDocument() { return payerDocument; }
    public void setPayerDocument(String payerDocument) { this.payerDocument = payerDocument; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public String getProviderCheckoutId() { return providerCheckoutId; }
    public void setProviderCheckoutId(String providerCheckoutId) { this.providerCheckoutId = providerCheckoutId; }

    public String getProviderMerchantId() { return providerMerchantId; }
    public void setProviderMerchantId(String providerMerchantId) { this.providerMerchantId = providerMerchantId; }

    public String getProviderTransactionId() { return providerTransactionId; }
    public void setProviderTransactionId(String providerTransactionId) { this.providerTransactionId = providerTransactionId; }

    public Integer getProviderResultCode() { return providerResultCode; }
    public void setProviderResultCode(Integer providerResultCode) { this.providerResultCode = providerResultCode; }

    public String getProviderResultDesc() { return providerResultDesc; }
    public void setProviderResultDesc(String providerResultDesc) { this.providerResultDesc = providerResultDesc; }

    public Map<String, Object> getRequestPayload() { return requestPayload; }
    public void setRequestPayload(Map<String, Object> requestPayload) { this.requestPayload = requestPayload; }

    public Map<String, Object> getResponsePayload() { return responsePayload; }
    public void setResponsePayload(Map<String, Object> responsePayload) { this.responsePayload = responsePayload; }

    public Map<String, Object> getCallbackPayload() { return callbackPayload; }
    public void setCallbackPayload(Map<String, Object> callbackPayload) { this.callbackPayload = callbackPayload; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    // ==================== MÉTODOS DE CICLO DE VIDA ====================

    @PrePersist
    protected void prePersist() {
        if (retryCount == null) retryCount = 0;
        if (feeAmount == null) feeAmount = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (currency == null) currency = "MZN";
        calculateNetAmount();
    }

    @PreUpdate
    protected void preUpdate() {
        calculateNetAmount();
    }

    private void calculateNetAmount() {
        if (amount != null) {
            BigDecimal net = amount;
            if (feeAmount != null) net = net.subtract(feeAmount);
            if (discountAmount != null) net = net.subtract(discountAmount);
            this.netAmount = net;
        }
    }

    // ==================== MÉTODOS DE STATUS ====================

    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isProcessing() { return "PROCESSING".equals(status); }
    public boolean isSuccess() { return "SUCCESS".equals(status); }
    public boolean isFailed() { return "FAILED".equals(status); }
    public boolean isRefunded() { return "REFUNDED".equals(status); }
    public boolean isExpired() { return expiresAt != null && expiresAt.isBefore(LocalDateTime.now()); }

    // ==================== BUILDER ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String transactionId;
        private String externalId;
        private Long saleId;
        private Long eventId;
        private Long userId;
        private String orderId;
        private PaymentMethodEntity method;
        private PaymentProviderConfigEntity provider;
        private BigDecimal amount;
        private BigDecimal feeAmount;
        private BigDecimal discountAmount;
        private String currency;
        private String payerName;
        private String payerEmail;
        private String payerPhone;
        private String payerDocument;
        private String status;
        private Integer statusCode;
        private String statusMessage;
        private String providerCheckoutId;
        private String providerMerchantId;
        private String providerTransactionId;
        private Integer providerResultCode;
        private String providerResultDesc;
        private Map<String, Object> requestPayload;
        private Map<String, Object> responsePayload;
        private Map<String, Object> callbackPayload;
        private Integer retryCount;
        private Integer maxRetries;
        private LocalDateTime processedAt;
        private LocalDateTime completedAt;
        private LocalDateTime expiresAt;

        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public Builder externalId(String externalId) { this.externalId = externalId; return this; }
        public Builder saleId(Long saleId) { this.saleId = saleId; return this; }
        public Builder eventId(Long eventId) { this.eventId = eventId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder orderId(String orderId) { this.orderId = orderId; return this; }
        public Builder method(PaymentMethodEntity method) { this.method = method; return this; }
        public Builder provider(PaymentProviderConfigEntity provider) { this.provider = provider; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder feeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; return this; }
        public Builder discountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder payerName(String payerName) { this.payerName = payerName; return this; }
        public Builder payerEmail(String payerEmail) { this.payerEmail = payerEmail; return this; }
        public Builder payerPhone(String payerPhone) { this.payerPhone = payerPhone; return this; }
        public Builder payerDocument(String payerDocument) { this.payerDocument = payerDocument; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder statusCode(Integer statusCode) { this.statusCode = statusCode; return this; }
        public Builder statusMessage(String statusMessage) { this.statusMessage = statusMessage; return this; }
        public Builder providerCheckoutId(String providerCheckoutId) { this.providerCheckoutId = providerCheckoutId; return this; }
        public Builder providerMerchantId(String providerMerchantId) { this.providerMerchantId = providerMerchantId; return this; }
        public Builder providerTransactionId(String providerTransactionId) { this.providerTransactionId = providerTransactionId; return this; }
        public Builder providerResultCode(Integer providerResultCode) { this.providerResultCode = providerResultCode; return this; }
        public Builder providerResultDesc(String providerResultDesc) { this.providerResultDesc = providerResultDesc; return this; }
        public Builder requestPayload(Map<String, Object> requestPayload) { this.requestPayload = requestPayload; return this; }
        public Builder responsePayload(Map<String, Object> responsePayload) { this.responsePayload = responsePayload; return this; }
        public Builder callbackPayload(Map<String, Object> callbackPayload) { this.callbackPayload = callbackPayload; return this; }
        public Builder retryCount(Integer retryCount) { this.retryCount = retryCount; return this; }
        public Builder maxRetries(Integer maxRetries) { this.maxRetries = maxRetries; return this; }
        public Builder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }

        public PaymentTransactionEntity build() {
            return new PaymentTransactionEntity(this);
        }
    }
}