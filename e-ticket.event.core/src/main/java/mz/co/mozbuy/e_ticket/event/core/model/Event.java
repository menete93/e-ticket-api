package mz.co.mozbuy.e_ticket.event.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // 🔥 NOVO IMPORT

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event extends AuditableEntity<Long, String> {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Event description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;

    /**
     * Campo geográfico compatível com PostGIS (GEOGRAPHY(POINT, 4326)).
     * Necessita da dependência hibernate-spatial.
     */
    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    @NotNull(message = "Geographic location is required")
    private Point geographicLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    @JsonIgnore  // ⚠️ Ignora esta propriedade na serialização
    private EventCategory category;

    // 🔥🔥🔥 NOVO: RELACIONAMENTO COM ORGANIZADOR 🔥🔥🔥
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    @NotNull(message = "Organizer is required")
    @JsonIgnore
    private Organizer organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EventTicket> tickets = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @Column(name = "min_attendees")
    private Integer minAttendees;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "is_free", nullable = false)
    private Boolean isFree = false;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "total_tickets")
    private Integer totalTickets = 0;

    @Column(name = "available_tickets")
    private Integer availableTickets = 0;

    @Column(name = "sold_tickets")
    private Integer soldTickets = 0;

    @Column(name = "reserved_tickets")
    private Integer reservedTickets = 0;

    // 🔥🔥🔥 NOVOS CAMPOS PARA ESTRATÉGIA HÍBRIDA 🔥🔥🔥

    // Configuração de comissão específica para este evento (sobrescreve a do organizador)
    @Column(name = "event_commission_rate", precision = 5, scale = 4)
    private BigDecimal eventCommissionRate;

    @Column(name = "event_flat_fee", precision = 10, scale = 2)
    private BigDecimal eventFlatFee;

    // Controle do período de trial (promoção de lançamento)
    @Column(name = "is_trial_event")
    private Boolean isTrialEvent = false;

    // Estatísticas financeiras
    @Column(name = "total_commission", precision = 15, scale = 2)
    private BigDecimal totalCommission = BigDecimal.ZERO;

    @Column(name = "total_organizer_payout", precision = 15, scale = 2)
    private BigDecimal totalOrganizerPayout = BigDecimal.ZERO;

    @Column(name = "total_sales", precision = 15, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;

    // 🔥🔥🔥 NOVOS RELACIONAMENTOS 🔥🔥🔥

    // Cupons de desconto para este evento
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DiscountCoupon> discountCoupons = new ArrayList<>();

    // Histórico de vendas deste evento
    @OneToMany(mappedBy = "event")
    @JsonIgnore
    private List<TicketSale> sales = new ArrayList<>();


    // ==================== CAMPOS DE CANCELAMENTO ====================

    @Column(name = "is_cancelled")
    private Boolean isCancelled = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "refund_processed")
    private Boolean refundProcessed = false;

    // ... resto do código ...
    // Adicione estes métodos na seção de métodos existentes

    public boolean isCancellable() {
        // Não pode cancelar se:
        // 1. Já foi cancelado
        // 2. Já ocorreu
        return !Boolean.TRUE.equals(this.isCancelled) &&
                (this.eventDate == null || this.eventDate.isAfter(LocalDateTime.now()));
    }

    public String getCancellationStatus() {
        if (Boolean.TRUE.equals(this.isCancelled)) {
            return "CANCELLED";
        }
        if (this.eventDate != null && this.eventDate.isBefore(LocalDateTime.now())) {
            return "COMPLETED";
        }
        return "ACTIVE";
    }

    // 🔥🔥🔥 CONSTRUTORES 🔥🔥🔥

    public Event() {}

    public Event(String name, String description, Point geographicLocation, EventCategory category, LocalDateTime eventDate) {
        this.name = name;
        this.description = description;
        this.geographicLocation = geographicLocation;
        this.category = category;
        this.eventDate = eventDate;
    }

    // 🔥🔥🔥 NOVO: Construtor com organizador 🔥🔥🔥
    public Event(String name, String description, Point geographicLocation,
                 EventCategory category, LocalDateTime eventDate, Organizer organizer) {
        this.name = name;
        this.description = description;
        this.geographicLocation = geographicLocation;
        this.category = category;
        this.eventDate = eventDate;
        this.organizer = organizer;
    }

    // 🔥🔥🔥 MÉTODOS EXISTENTES 🔥🔥🔥

    public void addTicket(EventTicket ticket) {
        tickets.add(ticket);
        ticket.setEvent(this);
        updateTicketStatistics();
    }

    public void removeTicket(EventTicket ticket) {
        tickets.remove(ticket);
        ticket.setEvent(null);
        updateTicketStatistics();
    }

    public void updateTicketStatistics() {
        this.totalTickets = tickets.stream().mapToInt(EventTicket::getTotalQuantity).sum();
        this.availableTickets = tickets.stream().mapToInt(EventTicket::getAvailableQuantity).sum();
        this.soldTickets = tickets.stream().mapToInt(EventTicket::getSoldQuantity).sum();
        this.reservedTickets = tickets.stream().mapToInt(EventTicket::getReservedQuantity).sum();
    }

    public boolean hasAvailableTickets() {
        return availableTickets > 0;
    }

    public boolean isRegistrationOpen() {
        return registrationDeadline == null || LocalDateTime.now().isBefore(registrationDeadline);
    }

    public boolean isEventActive() {
        return eventDate == null || LocalDateTime.now().isBefore(eventDate);
    }

    // 🔥🔥🔥 NOVOS MÉTODOS PARA ESTRATÉGIA HÍBRIDA 🔥🔥🔥

    /**
     * Obtém a taxa de comissão efetiva para este evento.
     * Prioriza: 1) taxa específica do evento, 2) taxa do organizador, 3) padrão 5%
     */
    public BigDecimal getEffectiveCommissionRate() {
        if (this.eventCommissionRate != null) {
            return this.eventCommissionRate;
        }
        if (this.organizer != null && this.organizer.getCommissionRate() != null) {
            return this.organizer.getCommissionRate();
        }
        return new BigDecimal("0.05"); // Padrão 5%
    }

    /**
     * Obtém a taxa fixa efetiva por bilhete.
     * Prioriza: 1) taxa específica do evento, 2) taxa do organizador, 3) padrão R$1,50
     */
    public BigDecimal getEffectiveFlatFee() {
        if (this.eventFlatFee != null) {
            return this.eventFlatFee;
        }
        if (this.organizer != null && this.organizer.getFlatFeePerTicket() != null) {
            return this.organizer.getFlatFeePerTicket();
        }
        return new BigDecimal("1.50"); // Padrão R$1,50
    }

    /**
     * Verifica se este evento é elegível para período de trial (0% comissão)
     */
    public boolean isEligibleForTrial() {
        if (this.organizer == null) {
            return false;
        }

        // Organizador ainda tem créditos de trial?
        boolean hasTrialRemaining = this.organizer.isEventEligibleForTrial();

        // Este evento foi marcado como trial?
        boolean isMarkedAsTrial = Boolean.TRUE.equals(this.isTrialEvent);

        boolean isFirstEvent = isFirstEventForOrganizer();

        return hasTrialRemaining && (isMarkedAsTrial || isFirstEvent);
    }

    public boolean isFirstEventForOrganizer() {
        if (this.organizer == null || this.organizer.getEvents() == null) {
            return false;
        }

        // Contar eventos do organizador (excluindo este)
        long otherEventsCount = this.organizer.getEvents().stream()
                .filter(e -> e != null && !e.getId().equals(this.getId()))
                .count();

        return otherEventsCount == 0;
    }

    /**
     * Marca este evento como tendo usado o crédito de trial
     */
    public void markAsTrialUsed() {
        this.isTrialEvent = false;
        if (this.organizer != null) {
            this.organizer.consumeTrialEvent();
        }
    }

    /**
     * Atualiza estatísticas financeiras após uma venda
     */
    public void updateFinancialStats(BigDecimal saleAmount, BigDecimal commission, BigDecimal payout) {
        // Inicializar se necessário
        if (this.totalSales == null) this.totalSales = BigDecimal.ZERO;
        if (this.totalCommission == null) this.totalCommission = BigDecimal.ZERO;
        if (this.totalOrganizerPayout == null) this.totalOrganizerPayout = BigDecimal.ZERO;

        // Atualizar valores
        this.totalSales = this.totalSales.add(saleAmount);
        this.totalCommission = this.totalCommission.add(commission);
        this.totalOrganizerPayout = this.totalOrganizerPayout.add(payout);
    }

    /**
     * Adiciona um cupom de desconto a este evento
     */
    public void addDiscountCoupon(DiscountCoupon coupon) {
        if (this.discountCoupons == null) {
            this.discountCoupons = new ArrayList<>();
        }
        this.discountCoupons.add(coupon);
        coupon.setEvent(this);
    }

    /**
     * Encontra um cupom válido pelo código
     */
    public DiscountCoupon findValidCoupon(String code) {
        if (this.discountCoupons == null || code == null || code.trim().isEmpty()) {
            return null;
        }

        return this.discountCoupons.stream()
                .filter(coupon -> code.equalsIgnoreCase(coupon.getCode()))
                .filter(DiscountCoupon::isValid)
                .findFirst()
                .orElse(null);
    }

    /**
     * Calcula a receita atual do evento (apenas vendas pagas)
     */
    public BigDecimal calculateCurrentRevenue() {
        if (this.sales == null || this.sales.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return this.sales.stream()
                .filter(sale -> sale != null && "PAID".equals(sale.getStatus()))
                .map(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Verifica se o evento pode aceitar mais vendas
     */
    public boolean canAcceptMoreSales() {
        if (this.maxAttendees != null && this.maxAttendees > 0) {
            return this.soldTickets < this.maxAttendees;
        }
        return true;
    }

    /**
     * Calcula a comissão para uma venda específica
     */
    public BigDecimal calculateCommissionForSale(BigDecimal saleAmount, Integer quantity) {
        if (this.isEligibleForTrial()) {
            return BigDecimal.ZERO; // Trial: 0% de comissão
        }

        // ESTRATÉGIA HÍBRIDA: Taxa fixa + Percentual
        BigDecimal commission = BigDecimal.ZERO;

        // 1. Taxa fixa por bilhete
        BigDecimal flatFee = this.getEffectiveFlatFee();
        if (flatFee.compareTo(BigDecimal.ZERO) > 0) {
            commission = commission.add(flatFee.multiply(BigDecimal.valueOf(quantity)));
        }

        // 2. Percentual sobre o total
        BigDecimal commissionRate = this.getEffectiveCommissionRate();
        if (commissionRate.compareTo(BigDecimal.ZERO) > 0) {
            commission = commission.add(saleAmount.multiply(commissionRate));
        }

        return commission.max(BigDecimal.ZERO);
    }

    /**
     * Método auxiliar: cria evento marcado como trial se organizador for elegível
     */
    public static Event createTrialEvent(String name, String description, Point geographicLocation,
                                         EventCategory category, LocalDateTime eventDate,
                                         Organizer organizer) {
        Event event = new Event(name, description, geographicLocation, category, eventDate, organizer);

        if (organizer.isEventEligibleForTrial()) {
            event.setIsTrialEvent(true);
        }

        return event;
    }

    /**
     * Obtém o payout para o organizador após deduzir comissão
     */
    public BigDecimal calculateOrganizerPayout(BigDecimal saleAmount, BigDecimal commission) {
        return saleAmount.subtract(commission != null ? commission : BigDecimal.ZERO)
                .max(BigDecimal.ZERO);
    }
}