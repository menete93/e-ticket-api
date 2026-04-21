package mz.co.mozbuy.e_ticket.event.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.CreateSaleDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.SaleResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceBreakdownItemDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceCalculationRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceCalculationResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.CommissionCalculation;
import mz.co.mozbuy.e_ticket.event.core.enums.SaleStatus;
import mz.co.mozbuy.e_ticket.event.core.exceptions.*;
import mz.co.mozbuy.e_ticket.event.core.model.*;
import mz.co.mozbuy.e_ticket.event.core.repository.*;
import mz.co.mozbuy.e_ticket.event.core.service.calculate.TicketPricingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketSaleService {

    private final EventTicketRepository eventTicketRepository;
    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final DiscountCouponRepository discountCouponRepository;
    private final TicketSaleRepository ticketSaleRepository;
    private final LoyaltyService loyaltyService;
    private final TicketPricingService ticketPricingService;
    private final ObjectMapper objectMapper;

    @Transactional
    public SaleResponseDTO createSale(CreateSaleDTO saleDTO) {
        log.info("📝 Creating sale for ticket: {}, quantity: {}, user: {}",
                saleDTO.getTicketId(), saleDTO.getQuantity(), saleDTO.getUserId());

        // 1. Buscar ticket com LOCK PESSIMISTA
        EventTicket ticket = eventTicketRepository.findByIdWithLock(saleDTO.getTicketId())
                .orElseThrow(() -> new TicketNotFoundException(saleDTO.getTicketId()));

        // 2. Validar ticket para venda
        validateTicketForSale(ticket, saleDTO.getQuantity());

        Event event = ticket.getEvent();
        Organizer organizer = event.getOrganizer();

        // 3. Calcular preço com as estratégias
        PriceCalculationResponseDTO priceCalculation = calculatePriceWithStrategies(saleDTO, ticket, event);

        // 4. Validar preço esperado (se fornecido)
        if (saleDTO.getExpectedTotalAmount() != null) {
            validateExpectedPrice(saleDTO.getExpectedTotalAmount(), priceCalculation.getFinalPrice());
        }

        // 5. Aplicar cupom se fornecido
        DiscountCoupon coupon = null;
        BigDecimal couponDiscount = BigDecimal.ZERO;
        BigDecimal finalAmount = priceCalculation.getFinalPrice();

        if (saleDTO.getCouponCode() != null && !saleDTO.getCouponCode().trim().isEmpty()) {
            coupon = applyCoupon(saleDTO.getCouponCode(), event, priceCalculation.getSubtotal());
            if (coupon != null) {
                couponDiscount = coupon.applyDiscount(priceCalculation.getSubtotal());
                finalAmount = priceCalculation.getSubtotal().subtract(couponDiscount);
                coupon.useCoupon();
                log.info("🎟️ Coupon applied: {}, discount: {}", coupon.getCode(), couponDiscount);
            }
        }

        // 6. Calcular comissão
        CommissionCalculation commission = calculateCommission(
                finalAmount, organizer, event, saleDTO.getQuantity());

        // 7. Criar registro de venda com o modelo TicketSale
        TicketSale sale = createTicketSale(
                saleDTO,
                ticket,
                event,
                organizer,
                coupon,
                priceCalculation,
                couponDiscount,
                finalAmount,
                commission
        );

        // 8. Atualizar estoque
        updateTicketInventory(ticket, saleDTO.getQuantity());

        // 9. Atualizar estatísticas financeiras
        updateFinancialStats(event, finalAmount, commission.amount(), commission.payout());

        // 10. Atualizar organizador
        updateOrganizerStats(organizer, finalAmount, commission.amount(), saleDTO.getQuantity());

        // 11. Se for evento trial, marcar como usado
        if (commission.isTrial()) {
            organizer.consumeTrialEvent();
        }

        // 12. Atualizar histórico de fidelidade
        if (saleDTO.getUserId() != null) {
            loyaltyService.updateCustomerHistory(
                    saleDTO.getUserId(),
                    ticket,
                    saleDTO.getQuantity(),
                    finalAmount,
                    event
            );
        }

        // 13. Salvar todas as alterações
        eventTicketRepository.save(ticket);
        if (coupon != null) {
            discountCouponRepository.save(coupon);
        }
        eventRepository.save(event);
        organizerRepository.save(organizer);
        TicketSale savedSale = ticketSaleRepository.save(sale);

        log.info("✅ Sale created successfully. Transaction ID: {}, Total: {}, Discounts: {}",
                savedSale.getTransactionId(), finalAmount, savedSale.getTotalDiscount());

        return SaleResponseDTO.fromEntity(savedSale);
    }

    /**
     * Cria o objeto TicketSale com todos os dados
     */
    private TicketSale createTicketSale(
            CreateSaleDTO saleDTO,
            EventTicket ticket,
            Event event,
            Organizer organizer,
            DiscountCoupon coupon,
            PriceCalculationResponseDTO priceCalculation,
            BigDecimal couponDiscount,
            BigDecimal finalAmount,
            CommissionCalculation commission) {

        // Calcular preço unitário baseado no original
        BigDecimal originalPrice = ticket.getOriginalPrice() != null ?
                ticket.getOriginalPrice() : ticket.getCurrentPrice();

        BigDecimal subtotal = originalPrice.multiply(BigDecimal.valueOf(saleDTO.getQuantity()));

        TicketSale sale = TicketSale.builder()
                // Identificação
                .transactionId(generateTransactionId())

                // Relacionamentos
                .event(event)
                .ticket(ticket)
                .organizer(organizer)
                .discountCoupon(coupon)
                .userId(saleDTO.getUserId())

                // Quantidade e preços
                .quantity(saleDTO.getQuantity())
                .unitPrice(originalPrice)
                .subtotal(subtotal)
                .discountAmount(couponDiscount)
                .totalAmount(finalAmount)

                // Comissão
                .commissionRate(commission.rate())
                .commissionAmount(commission.amount())
                .organizerPayout(commission.payout())

                // Dados do comprador
                .buyerEmail(saleDTO.getBuyerEmail())
                .buyerName(saleDTO.getBuyerName())
                .buyerPhone(saleDTO.getBuyerPhone())

                // Status
                .status(SaleStatus.PENDING)

                // Flags
                .isTrialEvent(commission.isTrial())

                // UTMs (capturados automaticamente)
                .utmSource(saleDTO.getUtmSource())
                .utmMedium(saleDTO.getUtmMedium())
                .utmCampaign(saleDTO.getUtmCampaign())

                .build();

        // Adicionar estratégias aplicadas (usa o método de conveniência)
        if (priceCalculation.getAppliedStrategies() != null &&
                !priceCalculation.getAppliedStrategies().isEmpty()) {

            sale.addAppliedStrategies(priceCalculation.getAppliedStrategies());

            // Se houver desconto das estratégias, registrar
            BigDecimal strategiesDiscount = priceCalculation.getSubtotal()
                    .subtract(priceCalculation.getDiscountedPrice());
            sale.setTotalDiscountFromStrategies(strategiesDiscount);
        }

        return sale;
    }

    /**
     * Calcula preço com estratégias
     */
    private PriceCalculationResponseDTO calculatePriceWithStrategies(
            CreateSaleDTO saleDTO,
            EventTicket ticket,
            Event event) {

        Map<Long, Integer> quantities = new HashMap<>();
        quantities.put(ticket.getId(), saleDTO.getQuantity());

        PriceCalculationRequestDTO request = PriceCalculationRequestDTO.builder()
                .eventId(event.getId())
                .ticketQuantities(quantities)
                .userId(saleDTO.getUserId())
                .email(saleDTO.getBuyerEmail())
                .build();

        return ticketPricingService.calculatePrice(request);
    }

    /**
     * Valida preço esperado
     */
    private void validateExpectedPrice(BigDecimal expected, BigDecimal calculated) {
        BigDecimal difference = expected.subtract(calculated).abs();
        BigDecimal tolerance = new BigDecimal("0.01");

        if (difference.compareTo(tolerance) > 0) {
            log.error("⚠️ PREÇO INCONSISTENTE! Esperado: {}, Calculado: {}", expected, calculated);
            throw new RuntimeException("Preço inconsistente com as estratégias aplicáveis");
        }
    }

    /**
     * Valida ticket para venda
     */
    private void validateTicketForSale(EventTicket ticket, Integer requestedQuantity) {
        if (!ticket.getLifeCycleState().equals(LifeCycleState.ACTIVE)) {
            throw new TicketNotActiveException();
        }

        if (!ticket.isSalesPeriodActive()) {
            throw new SalesPeriodNotActiveException();
        }

        if (ticket.getAvailableQuantity() < requestedQuantity) {
            throw new InsufficientTicketsException();
        }
    }

    /**
     * Aplica cupom
     */
    private DiscountCoupon applyCoupon(String couponCode, Event event, BigDecimal purchaseAmount) {
        DiscountCoupon coupon = discountCouponRepository
                .findByCodeAndEventId(couponCode, event.getId())
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + couponCode));

        if (!coupon.isValid()) {
            throw new RuntimeException("Coupon is not valid");
        }

        if (!coupon.canApplyToPurchase(purchaseAmount)) {
            throw new RuntimeException("Coupon cannot be applied to this purchase amount");
        }

        return coupon;
    }

    /**
     * Calcula comissão
     */
    private CommissionCalculation calculateCommission(BigDecimal totalAmount, Organizer organizer,
                                                      Event event, Integer quantity) {
        boolean isTrialEvent = event.isEligibleForTrial();

        if (isTrialEvent) {
            return new CommissionCalculation(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    totalAmount,
                    true
            );
        } else {
            BigDecimal commissionRate = event.getEffectiveCommissionRate();
            BigDecimal flatFee = event.getEffectiveFlatFee();

            BigDecimal flatFeeCommission = flatFee.multiply(BigDecimal.valueOf(quantity));
            BigDecimal percentageCommission = totalAmount.multiply(commissionRate);
            BigDecimal totalCommission = flatFeeCommission.add(percentageCommission);
            BigDecimal payout = totalAmount.subtract(totalCommission);

            return new CommissionCalculation(
                    commissionRate,
                    totalCommission,
                    payout,
                    false
            );
        }
    }

    /**
     * Atualiza inventário do ticket
     */
    private void updateTicketInventory(EventTicket ticket, Integer quantity) {
        if (ticket.getAvailableQuantity() < quantity) {
            throw new IllegalStateException(
                    String.format("Concurrency issue detected! Ticket %d: available %d, requested %d",
                            ticket.getId(), ticket.getAvailableQuantity(), quantity)
            );
        }

        int newSoldQuantity = ticket.getSoldQuantity() + quantity;
        int newAvailableQuantity = ticket.getAvailableQuantity() - quantity;

        ticket.setSoldQuantity(newSoldQuantity);
        ticket.setAvailableQuantity(newAvailableQuantity);

        Event event = ticket.getEvent();
        if (event != null) {
            event.setSoldTickets(event.getSoldTickets() + quantity);
            event.setAvailableTickets(event.getAvailableTickets() - quantity);
            event.updateTicketStatistics();
        }
    }

    /**
     * Atualiza estatísticas financeiras
     */
    private void updateFinancialStats(Event event, BigDecimal saleAmount,
                                      BigDecimal commission, BigDecimal payout) {
        event.updateFinancialStats(saleAmount, commission, payout);
        event.updateTicketStatistics();
    }

    /**
     * Atualiza estatísticas do organizador
     */
    private void updateOrganizerStats(Organizer organizer, BigDecimal saleAmount,
                                      BigDecimal commission, Integer quantity) {
        if (organizer == null) return;

        organizer.addEarnings(saleAmount);

        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            organizer.addCommissionPaid(commission);
        }

        organizer.incrementTicketsSold(quantity);
    }

    /**
     * Gera ID de transação único
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() +
                "-" + System.currentTimeMillis();
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleByTransactionId(String transactionId) {
        TicketSale sale = ticketSaleRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Sale not found with transactionId: " + transactionId));
        return SaleResponseDTO.fromEntity(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByEventId(Long eventId) {
        return ticketSaleRepository.findByEventId(eventId).stream()
                .map(SaleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByOrganizerId(Long organizerId) {
        return ticketSaleRepository.findByOrganizerId(organizerId).stream()
                .map(SaleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double calculatePriceWithCoupon(Long ticketId, Integer quantity, String couponCode) {
        EventTicket ticket = eventTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        Event event = ticket.getEvent();
        BigDecimal originalPrice = ticket.getOriginalPrice() != null ?
                ticket.getOriginalPrice() : ticket.getCurrentPrice();
        BigDecimal subtotal = originalPrice.multiply(BigDecimal.valueOf(quantity));

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            DiscountCoupon coupon = discountCouponRepository
                    .findByCodeAndEventId(couponCode, event.getId())
                    .orElseThrow(() -> new RuntimeException("Coupon not found: " + couponCode));

            if (coupon.isValid() && coupon.canApplyToPurchase(subtotal)) {
                discountAmount = coupon.applyDiscount(subtotal);
            }
        }

        return subtotal.subtract(discountAmount).doubleValue();
    }

    // ==================== MÉTODOS DE GESTÃO ====================

    @Transactional
    public SaleResponseDTO processPayment(String transactionId, String paymentMethod,
                                          String paymentReference) {
        TicketSale sale = ticketSaleRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + transactionId));

        sale.markAsPaid(paymentMethod, paymentReference);

        Organizer organizer = sale.getOrganizer();
        organizer.addToBalance(sale.getOrganizerPayout());

        ticketSaleRepository.save(sale);
        organizerRepository.save(organizer);

        log.info("Payment processed for transaction: {}, Amount: {}",
                transactionId, sale.getTotalAmount());

        return SaleResponseDTO.fromEntity(sale);
    }

    @Transactional
    public SaleResponseDTO cancelSale(String transactionId, String reason) {
        TicketSale sale = ticketSaleRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + transactionId));

        if (!sale.isCancellable()) {
            throw new RuntimeException("Sale cannot be cancelled in current status: " + sale.getStatus());
        }

        sale.markAsCancelled(reason);

        // Restaurar estoque
        EventTicket ticket = sale.getTicket();
        ticket.setSoldQuantity(ticket.getSoldQuantity() - sale.getQuantity());
        ticket.setAvailableQuantity(ticket.getAvailableQuantity() + sale.getQuantity());

        // Atualizar evento
        Event event = sale.getEvent();
        event.setSoldTickets(event.getSoldTickets() - sale.getQuantity());
        event.setAvailableTickets(event.getAvailableTickets() + sale.getQuantity());

        // Reverter financeiro se já estava pago
        if (sale.isPaid()) {
            event.updateFinancialStats(
                    sale.getTotalAmount().negate(),
                    sale.getCommissionAmount().negate(),
                    sale.getOrganizerPayout().negate()
            );

            Organizer organizer = sale.getOrganizer();
            organizer.addToBalance(sale.getOrganizerPayout().negate());
            organizer.addEarnings(sale.getTotalAmount().negate());
            organizer.addCommissionPaid(sale.getCommissionAmount().negate());
            organizer.incrementTicketsSold(-sale.getQuantity());

            organizerRepository.save(organizer);
        }

        ticketSaleRepository.save(sale);
        eventTicketRepository.save(ticket);
        eventRepository.save(event);

        log.info("Sale cancelled: {}, Reason: {}", transactionId, reason);

        return SaleResponseDTO.fromEntity(sale);
    }
}