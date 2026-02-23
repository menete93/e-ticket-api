package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.e_ticket.event.core.enums.SaleStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_sales")
@Getter
@Setter
public class TicketSale extends AuditableEntity<Long, String> {

    @Column(name = "transaction_id", nullable = false, unique = true, length = 50)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private EventTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private DiscountCoupon discountCoupon;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    // Informações de preço
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // Informações de comissão (ESTRATÉGIA HÍBRIDA)
    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", precision = 15, scale = 2)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    @Column(name = "organizer_payout", nullable = false, precision = 15, scale = 2)
    private BigDecimal organizerPayout;

    // Informações do comprador
    @Column(name = "buyer_email", length = 100)
    private String buyerEmail;

    @Column(name = "buyer_name", length = 200)
    private String buyerName;

    @Column(name = "buyer_phone", length = 20)
    private String buyerPhone;

    // Status da venda
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SaleStatus status = SaleStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "is_trial_event")
    private Boolean isTrialEvent = false;

    // Métodos de negócio
    public void calculateFinancials() {
        // Subtotal = preço unitário × quantidade
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));

        // Total = subtotal - desconto
        this.totalAmount = this.subtotal.subtract(
                this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO
        );

        // Organizador recebe = total - comissão
        this.organizerPayout = this.totalAmount.subtract(
                this.commissionAmount != null ? this.commissionAmount : BigDecimal.ZERO
        );
    }

    public void markAsPaid(String paymentMethod, String paymentReference) {
        // Salvar status anterior para lógica de reversão
        SaleStatus previousStatus = this.status;

        this.status = SaleStatus.PAID;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;

        // ✅ CORREÇÃO: Usar addCommissionPaid (não deductCommission)
        if (this.organizer != null && this.organizerPayout != null) {
            this.organizer.addToBalance(this.organizerPayout);
            this.organizer.addEarnings(this.totalAmount);

            // Só adiciona comissão se não for trial e for valor positivo
            if (this.commissionAmount != null &&
                    this.commissionAmount.compareTo(BigDecimal.ZERO) > 0 &&
                    !Boolean.TRUE.equals(this.isTrialEvent)) {
                this.organizer.addCommissionPaid(this.commissionAmount);
            }

            // Atualizar tickets vendidos
            this.organizer.incrementTicketsSold(this.quantity);
        }
    }

    public void markAsCancelled() {
        // Salvar status anterior para lógica de reversão
        SaleStatus previousStatus = this.status;

        this.status = SaleStatus.CANCELLED;

        // ✅ CORREÇÃO: Reverter apenas se estava PAID
        if (SaleStatus.PAID.equals(previousStatus) && this.organizer != null) {
            this.organizer.addToBalance(this.organizerPayout.negate());
            this.organizer.addEarnings(this.totalAmount.negate());

            // ✅ CORREÇÃO: Usar subtractCommissionPaid para remover comissão
            if (this.commissionAmount != null &&
                    this.commissionAmount.compareTo(BigDecimal.ZERO) > 0 &&
                    !Boolean.TRUE.equals(this.isTrialEvent)) {
                this.organizer.subtractCommissionPaid(this.commissionAmount);
            }

            // Reverter tickets vendidos
            this.organizer.decrementTicketsSold(this.quantity);
        }
    }

    // 🔥 ADICIONAR ESTES MÉTODOS ÚTEIS:

    public void markAsRefunded() {
        SaleStatus previousStatus = this.status;
        this.status = SaleStatus.REFUNDED;

        // Mesma lógica do cancelamento para reembolsos
        if (SaleStatus.PAID.equals(previousStatus) && this.organizer != null) {
            this.organizer.addToBalance(this.organizerPayout.negate());
            this.organizer.addEarnings(this.totalAmount.negate());

            if (this.commissionAmount != null &&
                    this.commissionAmount.compareTo(BigDecimal.ZERO) > 0 &&
                    !Boolean.TRUE.equals(this.isTrialEvent)) {
                this.organizer.subtractCommissionPaid(this.commissionAmount);
            }

            this.organizer.decrementTicketsSold(this.quantity);
        }
    }

    public boolean isPaid() {
        return SaleStatus.PAID.equals(this.status);
    }

    public boolean isPending() {
        return SaleStatus.PENDING.equals(this.status);
    }

    public boolean isCancelled() {
        return SaleStatus.CANCELLED.equals(this.status);
    }

    public boolean isRefunded() {
        return SaleStatus.REFUNDED.equals(this.status);
    }

    /**
     * Verifica se a venda pode ser cancelada
     */
    public boolean canBeCancelled() {
        return SaleStatus.PENDING.equals(this.status) ||
                SaleStatus.PAID.equals(this.status);
    }

    /**
     * Verifica se a venda pode ser reembolsada
     */
    public boolean canBeRefunded() {
        return SaleStatus.PAID.equals(this.status) &&
                !SaleStatus.REFUNDED.equals(this.status);
    }

    /**
     * Obtém informações resumidas da venda
     */
    public String getSaleSummary() {
        return String.format(
                "Venda %s | %d bilhetes | %s MZN | Status: %s | Trial: %s",
                this.transactionId,
                this.quantity,
                this.totalAmount != null ? this.totalAmount.toPlainString() : "0.00",
                this.status != null ? this.status.name() : "UNKNOWN",
                Boolean.TRUE.equals(this.isTrialEvent) ? "✅" : "❌"
        );
    }
}