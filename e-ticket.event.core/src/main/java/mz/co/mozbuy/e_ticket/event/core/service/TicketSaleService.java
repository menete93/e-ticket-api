package mz.co.mozbuy.e_ticket.event.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.CreateSaleDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.PaymentTransactionRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.SaleResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceCalculationRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceCalculationResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.CommissionCalculation;
import mz.co.mozbuy.e_ticket.event.core.enums.SaleStatus;
import mz.co.mozbuy.e_ticket.event.core.exceptions.*;
import mz.co.mozbuy.e_ticket.event.core.model.*;
import mz.co.mozbuy.e_ticket.event.core.repository.*;
import mz.co.mozbuy.e_ticket.event.core.service.calculate.TicketPricingService;
import mz.co.mozbuy.e_ticket.event.core.service.payment.HybridPaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
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
    private final HybridPaymentService hybridPaymentService;
    private final TicketSaleItemRepository ticketSaleItemRepository;

    @Transactional
    public SaleResponseDTO createSale(CreateSaleDTO saleDTO) {
        // ✅ Usar o método normalizado
        Map<Long, Integer> ticketQuantities = saleDTO.getNormalizedTicketQuantities();
        int totalQuantity = saleDTO.getTotalQuantity();

        log.info("📝 Creating sale for tickets: {}, user: {}", ticketQuantities, saleDTO.getUserId());

        // 1. Buscar e validar TODOS os tickets
        List<EventTicket> tickets = new ArrayList<>();
        Map<Long, EventTicket> ticketMap = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : ticketQuantities.entrySet()) {
            Long ticketId = entry.getKey();
            Integer quantity = entry.getValue();

            EventTicket ticket = eventTicketRepository.findByIdWithLock(ticketId)
                    .orElseThrow(() -> new TicketNotFoundException(ticketId));
            validateTicketForSale(ticket, quantity);

            tickets.add(ticket);
            ticketMap.put(ticketId, ticket);
        }

        // 2. Pegar o primeiro ticket para referências (evento, organizador)
        EventTicket firstTicket = tickets.get(0);
        Event event = firstTicket.getEvent();
        Organizer organizer = event.getOrganizer();

        // ✅ Guardar IDs para usar nas queries
        Long eventId = event.getId();
        Long organizerId = organizer.getId();

        // 3. Calcular preço total (já suporta múltiplos tickets!)
        PriceCalculationResponseDTO priceCalculation = calculatePriceWithStrategies(saleDTO, ticketQuantities, event);

        // 4. Validar preço esperado
        if (saleDTO.getExpectedTotalAmount() != null) {
            validateExpectedPrice(saleDTO.getExpectedTotalAmount(), priceCalculation.getFinalPrice());
        }

        // 5. Aplicar cupom
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
                finalAmount, organizer, event, totalQuantity);

        // 7. Criar registro de venda
        TicketSale sale = createTicketSale(saleDTO, ticketQuantities, event, organizer, coupon,
                priceCalculation, couponDiscount, finalAmount, commission, null);
        ticketSaleRepository.save(sale);

        // 8. Criar itens para cada ticket
        for (Map.Entry<Long, Integer> entry : ticketQuantities.entrySet()) {
            Long ticketId = entry.getKey();
            int qty = entry.getValue();

            EventTicket ticketItem = ticketMap.get(ticketId);
            BigDecimal originalPrice = ticketItem.getOriginalPrice() != null
                    ? ticketItem.getOriginalPrice()
                    : ticketItem.getCurrentPrice();
            BigDecimal subtotal = originalPrice.multiply(BigDecimal.valueOf(qty));

            // Calcular preço final do item (proporcional ao total)
            BigDecimal proportion = subtotal.divide(priceCalculation.getSubtotal(), 10, RoundingMode.HALF_EVEN);
            BigDecimal itemFinalPrice = finalAmount.multiply(proportion);
            BigDecimal itemDiscount = subtotal.subtract(itemFinalPrice);

            TicketSaleItem item = TicketSaleItem.builder()
                    .sale(sale)
                    .ticket(ticketItem)
                    .quantity(qty)
                    .unitPrice(originalPrice)
                    .finalPrice(itemFinalPrice)
                    .discountAmount(itemDiscount)
                    .build();
            ticketSaleItemRepository.save(item);
            sale.addItem(item);

            // Atualizar estoque do ticket
            updateTicketInventory(ticketItem, qty);
        }

        // ============================================================
        // 🔥 9. ATUALIZAR ESTATÍSTICAS DO EVENTO (USANDO QUERY DIRETA)
        // ============================================================
        // ❌ REMOVA: updateFinancialStats(event, finalAmount, commission.amount(), commission.payout());
        // ✅ Use query direta:
        eventRepository.updateFinancialAndTicketStats(
                eventId,                          // ID do evento
                finalAmount,                      // totalSales
                commission.amount(),              // totalCommission
                commission.payout(),              // totalOrganizerPayout
                totalQuantity                     // soldTickets +, availableTickets -
        );
        log.info("📊 Event stats updated: revenue={}, commission={}, payout={}, tickets={}",
                finalAmount, commission.amount(), commission.payout(), totalQuantity);

        // ============================================================
        // 🔥 10. ATUALIZAR ORGANIZADOR (USANDO QUERY DIRETA)
        // ============================================================
        // ❌ REMOVA: updateOrganizerStats(organizer, finalAmount, commission.amount(), totalQuantity);
        // ✅ Use query direta:
        organizerRepository.updateStats(
                organizerId,                      // ID do organizador
                finalAmount,                      // totalEarnings
                commission.amount(),              // totalCommissionPaid
                totalQuantity,                    // totalTicketsSold
                commission.payout()               // accountBalance
        );
        log.info("👤 Organizer stats updated: earnings={}, commission={}, tickets={}, balance={}",
                finalAmount, commission.amount(), totalQuantity, commission.payout());

        // ============================================================
        // 🔥 11. CONSUMIR TRIAL SE FOR O CASO
        // ============================================================
        // ❌ REMOVA: if (commission.isTrial()) { organizer.consumeTrialEvent(); }
        // ✅ Use query direta:
        if (commission.isTrial()) {
            int updated = organizerRepository.consumeTrialEvent(organizerId);
            if (updated == 0) {
                log.warn("⚠️ Trial event consumed failed for organizer: {}", organizerId);
            } else {
                log.info("🎯 Trial event consumed for organizer: {}", organizerId);
            }
        }

        // ============================================================
        // 🔥 12. SALVAR TICKETS (NECESSÁRIO - MANTÉM)
        // ============================================================
        for (EventTicket ticket : tickets) {
            eventTicketRepository.save(ticket);
        }

        // ============================================================
        // 🔥 13. ATUALIZAR HISTÓRICO DE FIDELIDADE (MANTÉM)
        // ============================================================
        if (saleDTO.getUserId() != null) {
            loyaltyService.updateCustomerHistory(
                    saleDTO.getUserId(),
                    firstTicket,
                    totalQuantity,
                    finalAmount,
                    event
            );
        }

        // ============================================================
        // 🔥 14. SALVAR CUPOM SE USADO (MANTÉM)
        // ============================================================
        if (coupon != null) {
            discountCouponRepository.save(coupon);
            log.info("🎟️ Coupon saved: {}", coupon.getCode());
        }

        // ============================================================
        // 🔥 15. ❌ REMOVER ESTAS LINHAS - NÃO SALVAR EVENTO E ORGANIZADOR
        // ============================================================
        // ❌ REMOVA: eventRepository.save(event);
        // ❌ REMOVA: organizerRepository.save(organizer);

        // ============================================================
        // 🔥 16. SALVAR VENDA (MANTÉM)
        // ============================================================
        TicketSale savedSale = ticketSaleRepository.save(sale);
        log.info("💾 Sale saved with ID: {}", savedSale.getId());

        // ============================================================
        // 🔥 17. CRIAR PAYMENT TRANSACTION (MANTÉM)
        // ============================================================
        log.info("💰 Criando PaymentTransaction para venda: {}", savedSale.getId());

        String reservationCode = generateReservationCode();

        PaymentTransactionRequestDTO paymentRequest = PaymentTransactionRequestDTO.builder()
                .reservationCode(reservationCode)
                .saleId(savedSale.getId())
                .eventId(eventId)                 // Use eventId, não event.getId()
                .userId(savedSale.getUserId())
                .amount(savedSale.getTotalAmount())
                .paymentMethodCode(saleDTO.getPaymentMethod())
                .currency("MZN")
                .quantity(savedSale.getQuantity())
                .build();

        PaymentTransactionEntity transaction = hybridPaymentService.createPaymentTransaction(paymentRequest);
        log.info("✅ PaymentTransaction criada: {}", transaction.getReservationCode());

        savedSale.setTransactionId(transaction.getReservationCode());
        ticketSaleRepository.save(savedSale);

        log.info("✅ Sale created successfully. Reservation Code: {}, Total: {}, Tickets: {}",
                savedSale.getTransactionId(), finalAmount, ticketQuantities);

        return SaleResponseDTO.fromEntity(savedSale);
    }    /**
     * Cria o objeto TicketSale com todos os dados
     */
    private TicketSale createTicketSale(
            CreateSaleDTO saleDTO,
            Map<Long, Integer> ticketQuantities,
            Event event,
            Organizer organizer,
            DiscountCoupon coupon,
            PriceCalculationResponseDTO priceCalculation,
            BigDecimal couponDiscount,
            BigDecimal finalAmount,
            CommissionCalculation commission,
            String transactionId) {

        // Calcular preço médio (para compatibilidade)
        BigDecimal averageUnitPrice = priceCalculation.getSubtotal()
                .divide(BigDecimal.valueOf(saleDTO.getTotalQuantity()), 2, RoundingMode.HALF_EVEN);

        TicketSale sale = TicketSale.builder()
                .transactionId(transactionId)
                .event(event)
                .organizer(organizer)
                .discountCoupon(coupon)
                .userId(saleDTO.getUserId())
                .quantity(saleDTO.getTotalQuantity())
                .unitPrice(averageUnitPrice)
                .subtotal(priceCalculation.getSubtotal())
                .discountAmount(couponDiscount)
                .totalAmount(finalAmount)
                .commissionRate(commission.rate())
                .commissionAmount(commission.amount())
                .organizerPayout(commission.payout())
                .buyerEmail(saleDTO.getBuyerEmail())
                .buyerName(saleDTO.getBuyerName())
                .buyerPhone(saleDTO.getBuyerPhone())
                .status(SaleStatus.PENDING)
                .isTrialEvent(commission.isTrial())
                .utmSource(saleDTO.getUtmSource())
                .utmMedium(saleDTO.getUtmMedium())
                .utmCampaign(saleDTO.getUtmCampaign())
                .build();

        if (priceCalculation.getAppliedStrategies() != null && !priceCalculation.getAppliedStrategies().isEmpty()) {
            sale.addAppliedStrategies(priceCalculation.getAppliedStrategies());
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
            Map<Long, Integer> ticketQuantities,
            Event event) {

        PriceCalculationRequestDTO request = PriceCalculationRequestDTO.builder()
                .eventId(event.getId())
                .ticketQuantities(ticketQuantities)
                .userId(saleDTO.getUserId())
                .email(saleDTO.getBuyerEmail())
                .couponCode(saleDTO.getCouponCode())
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
        if (!ticket.getState().equals(LifeCycleState.ACTIVE)) {
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
     * Gera código único para a reserva
     */
    private static final AtomicLong sequence = new AtomicLong(1);

    private String generateReservationCode() {
        long timestamp = System.currentTimeMillis() / 1000; // 10 dígitos
        String ts = String.valueOf(timestamp).substring(2); // pega 8 dígitos (posição 2 em diante)
        // timestamp = 1748537890
        // substring(2) = "48537890" (8 dígitos)
        String seq = String.format("%03d", sequence.getAndIncrement() % 1000); // 3 dígitos
        return ts + seq; // 8 + 3 = 11 dígitos ✅
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

        // ✅ CORRIGIDO: Restaurar estoque de TODOS os tickets nos itens
        for (TicketSaleItem item : sale.getItems()) {
            EventTicket ticket = item.getTicket();
            int quantity = item.getQuantity();

            ticket.setSoldQuantity(ticket.getSoldQuantity() - quantity);
            ticket.setAvailableQuantity(ticket.getAvailableQuantity() + quantity);

            eventTicketRepository.save(ticket);
        }

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
        eventRepository.save(event);

        log.info("Sale cancelled: {}, Reason: {}", transactionId, reason);

        return SaleResponseDTO.fromEntity(sale);
    }
}