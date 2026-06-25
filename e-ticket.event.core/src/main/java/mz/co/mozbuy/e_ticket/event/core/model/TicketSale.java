package mz.co.mozbuy.e_ticket.event.core.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceBreakdownItemDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_sales", indexes = {
        @Index(name = "idx_transaction_id", columnList = "transactionId"),
        @Index(name = "idx_event_id", columnList = "event_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status")
})
public class TicketSale extends AuditableEntity<Long, String> {

    @Column(unique = true, nullable = false, length = 50)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private DiscountCoupon discountCoupon;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", precision = 19, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "organizer_payout", precision = 19, scale = 2)
    private BigDecimal organizerPayout;

    @Column(name = "buyer_email", length = 100)
    private String buyerEmail;

    @Column(name = "buyer_name", length = 100)
    private String buyerName;

    @Column(name = "buyer_phone", length = 20)
    private String buyerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "is_trial_event")
    private Boolean isTrialEvent = false;


    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TicketSaleItem> items = new ArrayList<>();

    // ==================== CAMPOS DE ESTRATÉGIAS ====================

    @Column(name = "total_discount_from_strategies", precision = 19, scale = 2)
    private BigDecimal totalDiscountFromStrategies;

    @Column(name = "applied_strategies_json", columnDefinition = "TEXT")
    private String appliedStrategiesJson;

    // ==================== CAMPOS DE MARKETING ====================
    /**
     * Automático - Capturado da URL quando o usuário clica num link
     *Na prática: O organizador cria links com UTMs, compartilha no Facebook/Instagram/E-mail,
     * e quando o usuário compra, o sistema sabe exatamente de qual campanha veio!
     */
    @Column(name = "utm_source", length = 50)
    private String utmSource;

    @Column(name = "utm_medium", length = 50)
    private String utmMedium;

    @Column(name = "utm_campaign", length = 50)
    private String utmCampaign;

    // ==================== CAMPOS TRANSIENTES ====================

    @Transient
    private List<PriceBreakdownItemDTO> appliedStrategies;

    // ==================== OBJECT MAPPER CONFIGURADO ====================

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ==================== LIFECYCLE METHODS ====================

    /**
     * ✅ CORRIGIDO: Chamamos os setters diretamente em vez de super.onCreate()
     */
    @PrePersist
    public void prePersist() {
        // 👇 Em vez de super.onCreate(), setamos os campos manualmente
        if (getCreatedAt() == null) {
            setCreatedAt(LocalDateTime.now());
        }
        setUpdatedAt(LocalDateTime.now());

        // Serializar estratégias para JSON antes de salvar
        serializeStrategies();
    }

    @PreUpdate
    public void preUpdate() {
        setUpdatedAt(LocalDateTime.now());

        // Serializar estratégias para JSON antes de atualizar
        serializeStrategies();
    }

    @PostLoad
    public void postLoad() {
        // Desserializar estratégias do JSON após carregar
        deserializeStrategies();
    }

    // ==================== MÉTODOS DE UTILITÁRIOS ====================

    /**
     * Serializa a lista de estratégias para JSON
     */
    private void serializeStrategies() {
        if (appliedStrategies != null && !appliedStrategies.isEmpty()) {
            try {
                this.appliedStrategiesJson = objectMapper.writeValueAsString(appliedStrategies);
                log.debug("✅ Serialized {} strategies to JSON", appliedStrategies.size());
            } catch (JsonProcessingException e) {
                log.error("❌ Error serializing strategies to JSON", e);
                this.appliedStrategiesJson = "[]";
            }
        } else {
            this.appliedStrategiesJson = "[]";
        }
    }

    /**
     * Desserializa o JSON para lista de estratégias
     */
    private void deserializeStrategies() {
        if (appliedStrategiesJson != null && !appliedStrategiesJson.isEmpty() &&
                !appliedStrategiesJson.equals("[]")) {
            try {
                this.appliedStrategies = objectMapper.readValue(
                        appliedStrategiesJson,
                        new TypeReference<List<PriceBreakdownItemDTO>>() {}
                );
                log.debug("✅ Deserialized {} strategies from JSON", appliedStrategies.size());
            } catch (JsonProcessingException e) {
                log.error("❌ Error deserializing strategies from JSON", e);
                this.appliedStrategies = new ArrayList<>();
            }
        } else {
            this.appliedStrategies = new ArrayList<>();
        }
    }

    /**
     * Adiciona as estratégias aplicadas (método de conveniência)
     */
    public void addAppliedStrategies(List<PriceBreakdownItemDTO> strategies) {
        this.appliedStrategies = strategies;
        serializeStrategies();

        // Calcular desconto total das estratégias
        if (strategies != null && !strategies.isEmpty()) {
            this.totalDiscountFromStrategies = strategies.stream()
                    .map(PriceBreakdownItemDTO::getDiscountValue)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    // ==================== MÉTODOS DE NEGÓCIO ====================

    /**
     * Marca a venda como paga
     */
    public void markAsPaid(String paymentMethod, String paymentReference) {
        this.status = SaleStatus.PAID;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.paidAt = LocalDateTime.now();
        log.info("✅ Sale {} marked as PAID", this.transactionId);
    }

    /**
     * Marca a venda como cancelada
     */
    public void markAsCancelled() {
        this.status = SaleStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        log.info("❌ Sale {} marked as CANCELLED", this.transactionId);
    }

    /**
     * Marca a venda como cancelada com motivo
     */
    public void markAsCancelled(String reason) {
        this.status = SaleStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        log.info("❌ Sale {} marked as CANCELLED. Reason: {}", this.transactionId, reason);
    }

    /**
     * Verifica se a venda pode ser cancelada
     */
    public boolean isCancellable() {
        return this.status == SaleStatus.PENDING || this.status == SaleStatus.PAID;
    }

    /**
     * Verifica se a venda está paga
     */
    public boolean isPaid() {
        return this.status == SaleStatus.PAID;
    }

    /**
     * Verifica se a venda está pendente
     */
    public boolean isPending() {
        return this.status == SaleStatus.PENDING;
    }

    /**
     * Calcula o desconto total (cupom + estratégias)
     */
    public BigDecimal getTotalDiscount() {
        BigDecimal total = BigDecimal.ZERO;

        if (discountAmount != null) {
            total = total.add(discountAmount);
        }

        if (totalDiscountFromStrategies != null) {
            total = total.add(totalDiscountFromStrategies);
        }

        return total;
    }

    /**
     * Obtém o valor líquido (total - comissão)
     */
    public BigDecimal getNetAmount() {
        if (totalAmount != null && commissionAmount != null) {
            return totalAmount.subtract(commissionAmount);
        }
        return totalAmount;
    }

    public void addItem(TicketSaleItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.add(item);
        item.setSale(this);
    }

    public void removeItem(TicketSaleItem item) {
        items.remove(item);
        item.setSale(null);
    }

    public BigDecimal calculateTotalFromItems() {
        return items.stream()
                .map(TicketSaleItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<EventTicket> getAllTickets() {
        return items.stream()
                .map(TicketSaleItem::getTicket)
                .collect(Collectors.toList());
    }
}