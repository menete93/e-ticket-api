package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organizers")
@Getter
@Setter
public class Organizer extends AuditableEntity<Long, String> {

    // ============================================
    // 🔥 INFORMAÇÕES BÁSICAS (OBRIGATÓRIAS)
    // ============================================

    @Column(name = "reference_id", unique = true, nullable = false)
    private String referenceId = UUID.randomUUID().toString();

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;

    @Column(name = "phone_number", length = 20)
    @Pattern(
            regexp = "^(82|83|84|85|86|87)\\d{7}$",
            message = "Número inválido. Use um número válido de Moçambique (82–87)"
    )
    private String phoneNumber;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "nuit", length = 50)
    @Pattern(
            regexp = "^\\d{9}$",
            message = "NUIT inválido. Deve conter exatamente 9 dígitos"
    )
    private String nuit;

    // ============================================
    // 🔥 ESTRATÉGIA HÍBRIDA DE PRICING (CORE)
    // ============================================
    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate = new BigDecimal("0.05"); // 5% padrão

    @Column(name = "flat_fee_per_ticket", precision = 10, scale = 2)
    private BigDecimal flatFeePerTicket = new BigDecimal("1.50"); // MZN 1,50

    @Column(name = "trial_events_remaining")
    private Integer trialEventsRemaining = 3; // Eventos grátis no início

    @Column(name = "trial_used_count")
    private Integer trialUsedCount = 0;

    // ============================================
    // 🔥 ESTATÍSTICAS FINANCEIRAS
    // ============================================
    @Column(name = "account_balance", precision = 15, scale = 2)
    private BigDecimal accountBalance = BigDecimal.ZERO;

    @Column(name = "total_earnings", precision = 15, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "total_commission_paid", precision = 15, scale = 2)
    private BigDecimal totalCommissionPaid = BigDecimal.ZERO;

    @Column(name = "total_tickets_sold")
    private Integer totalTicketsSold = 0;

    @Column(name = "total_events_created")
    private Integer totalEventsCreated = 0;

    // ============================================
    // 🔥 RELACIONAMENTOS
    // ============================================
    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "organizer")
    private List<TicketSale> sales = new ArrayList<>();

    // ============================================
    // 🔥 MÉTODOS DE STATUS (LifeCycleState)
    // ============================================

    /**
     * Verifica se organizador está ATIVO
     */
    public boolean isActive() {
        return LifeCycleState.ACTIVE.equals(this.getLifeCycleState());
    }

    /**
     * Verifica se organizador está INATIVO
     */
    public boolean isInactive() {
        return LifeCycleState.INACTIVE.equals(this.getLifeCycleState());
    }

    /**
     * Verifica se organizador está BLOQUEADO
     */
    public boolean isBlocked() {
        return LifeCycleState.BLOCKED.equals(this.getLifeCycleState());
    }

    /**
     * Verifica se organizador está BANIDO
     */
    public boolean isBanned() {
        return LifeCycleState.BANNED.equals(this.getLifeCycleState());
    }

    /**
     * Verifica se organizador está DELETADO
     */
    public boolean isDeleted() {
        return LifeCycleState.DELETED.equals(this.getLifeCycleState());
    }

    /**
     * Verifica se organizador pode operar
     * (ativo e não bloqueado/banido)
     */
    public boolean canOperate() {
        LifeCycleState state = this.getLifeCycleState();
        return LifeCycleState.ACTIVE.equals(state) || LifeCycleState.INACTIVE.equals(state);
    }

    /**
     * Verifica se organizador pode criar eventos
     */
    public boolean canCreateEvents() {
        return canOperate() && !isBlocked() && !isBanned();
    }

    /**
     * Verifica se organizador pode receber pagamentos
     */
    public boolean canReceivePayments() {
        return isActive() && !isBlocked() && !isBanned();
    }

    // ============================================
    // 🔥 MÉTODOS DE TRANSIÇÃO DE ESTADO
    // ============================================

    public void activate() {
        this.setLifeCycleState(LifeCycleState.ACTIVE);
    }

    public void deactivate() {
        this.setLifeCycleState(LifeCycleState.INACTIVE);
    }

    public void block() {
        this.setLifeCycleState(LifeCycleState.BLOCKED);
    }

    public void unblock() {
        // Só pode desbloquear para ACTIVE ou INACTIVE
        if (isBlocked()) {
            this.setLifeCycleState(LifeCycleState.ACTIVE);
        }
    }

    public void ban() {
        this.setLifeCycleState(LifeCycleState.BANNED);
    }

    public void markAsDeleted() {
        this.setLifeCycleState(LifeCycleState.DELETED);
    }

    // ============================================
    // 🔥 MÉTODOS DE TRIAL (ESTRATÉGIA HÍBRIDA)
    // ============================================

    public boolean isEventEligibleForTrial() {
        return trialEventsRemaining != null && trialEventsRemaining > 0;
    }

    public void consumeTrialEvent() {
        if (isEventEligibleForTrial()) {
            trialEventsRemaining--;
            if (trialUsedCount == null) trialUsedCount = 0;
            trialUsedCount++;
        }
    }

    public BigDecimal calculateCommission(BigDecimal ticketPrice, Integer quantity) {
        // Se não pode operar, retorna 0
        if (!canReceivePayments()) {
            return BigDecimal.ZERO;
        }

        // Se tem trial disponível, comissão 0%
        if (isEventEligibleForTrial()) {
            return BigDecimal.ZERO;
        }

        // ESTRATÉGIA HÍBRIDA: Taxa fixa + percentual
        BigDecimal commission = BigDecimal.ZERO;

        // 1. Taxa fixa por bilhete
        if (flatFeePerTicket != null && flatFeePerTicket.compareTo(BigDecimal.ZERO) > 0) {
            commission = commission.add(flatFeePerTicket.multiply(BigDecimal.valueOf(quantity)));
        }

        // 2. Percentual sobre o total
        if (commissionRate != null && commissionRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalPrice = ticketPrice.multiply(BigDecimal.valueOf(quantity));
            commission = commission.add(totalPrice.multiply(commissionRate));
        }

        return commission.max(BigDecimal.ZERO);
    }

    // ============================================
    // 🔥 MÉTODOS FINANCEIROS
    // ============================================

    public void addToBalance(BigDecimal amount) {
        if (amount != null && canReceivePayments()) {
            if (this.accountBalance == null) this.accountBalance = BigDecimal.ZERO;
            this.accountBalance = this.accountBalance.add(amount);
        }
    }

    public void addCommissionPaid(BigDecimal commission) {
        if (commission != null && commission.compareTo(BigDecimal.ZERO) > 0) {
            if (this.totalCommissionPaid == null) this.totalCommissionPaid = BigDecimal.ZERO;
            this.totalCommissionPaid = this.totalCommissionPaid.add(commission);
        }
    }

    public void subtractCommissionPaid(BigDecimal commission) {
        if (commission != null && commission.compareTo(BigDecimal.ZERO) > 0) {
            if (this.totalCommissionPaid == null) this.totalCommissionPaid = BigDecimal.ZERO;
            this.totalCommissionPaid = this.totalCommissionPaid.subtract(commission);
        }
    }

    public void addEarnings(BigDecimal earnings) {
        if (earnings != null && canReceivePayments()) {
            if (this.totalEarnings == null) this.totalEarnings = BigDecimal.ZERO;
            this.totalEarnings = this.totalEarnings.add(earnings);
        }
    }

    public boolean withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (!canReceivePayments()) return false;
        if (accountBalance == null || accountBalance.compareTo(amount) < 0) return false;

        accountBalance = accountBalance.subtract(amount);
        return true;
    }

    // ============================================
    // 🔥 MÉTODOS DE ESTATÍSTICAS
    // ============================================

    public void incrementTicketsSold(Integer quantity) {
        if (quantity != null && quantity > 0 && canReceivePayments()) {
            if (this.totalTicketsSold == null) this.totalTicketsSold = 0;
            this.totalTicketsSold += quantity;
        }
    }

    public void decrementTicketsSold(Integer quantity) {
        if (quantity != null && quantity > 0) {
            if (this.totalTicketsSold == null) this.totalTicketsSold = 0;
            this.totalTicketsSold = Math.max(0, this.totalTicketsSold - quantity);
        }
    }

    public void incrementEventsCreated() {
        if (canCreateEvents()) {
            if (this.totalEventsCreated == null) this.totalEventsCreated = 0;
            this.totalEventsCreated++;
        }
    }

    public BigDecimal getNetEarnings() {
        BigDecimal earnings = totalEarnings != null ? totalEarnings : BigDecimal.ZERO;
        BigDecimal commission = totalCommissionPaid != null ? totalCommissionPaid : BigDecimal.ZERO;
        return earnings.subtract(commission);
    }

    // ============================================
    // 🔥 MÉTODOS DE RELATÓRIO/INFO
    // ============================================

    public String getStatusInfo() {
        LifeCycleState state = this.getLifeCycleState();
        String statusEmoji = "❓";
        String statusText = "DESCONHECIDO";

        switch (state) {
            case ACTIVE -> {
                statusEmoji = "🟢";
                statusText = "ATIVO";
            }
            case INACTIVE -> {
                statusEmoji = "⚪";
                statusText = "INATIVO";
            }
            case BLOCKED -> {
                statusEmoji = "🚫";
                statusText = "BLOQUEADO";
            }
            case BANNED -> {
                statusEmoji = "⛔";
                statusText = "BANIDO";
            }
            case DELETED -> {
                statusEmoji = "🗑️";
                statusText = "DELETADO";
            }
        }

        return String.format("%s %s | Trials: %d/%d | Saldo: %s MZN",
                statusEmoji, statusText,
                trialUsedCount != null ? trialUsedCount : 0,
                trialEventsRemaining != null ? trialEventsRemaining : 0,
                accountBalance != null ? accountBalance.toPlainString() : "0.00");
    }

    public String getFinancialSummary() {
        return String.format(
                "Receita Total: %s MZN | Comissão Paga: %s MZN | " +
                        "Receita Líquida: %s MZN | Bilhetes Vendidos: %d",
                totalEarnings != null ? totalEarnings.toPlainString() : "0.00",
                totalCommissionPaid != null ? totalCommissionPaid.toPlainString() : "0.00",
                getNetEarnings().toPlainString(),
                totalTicketsSold != null ? totalTicketsSold : 0
        );
    }

    // ============================================
    // 🔥 MÉTODOS DE VALIDAÇÃO
    // ============================================

    public boolean isValidForBusiness() {
        // Verifica se tem dados mínimos para operar
        boolean hasBasicInfo = name != null && !name.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();

        boolean canDoBusiness = canOperate() && !isDeleted();

        return hasBasicInfo && canDoBusiness;
    }

    public boolean canUseTrial() {
        return isActive() && isEventEligibleForTrial();
    }
}