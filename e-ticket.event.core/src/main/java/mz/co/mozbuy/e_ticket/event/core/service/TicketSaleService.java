package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.CreateSaleDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.SaleResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.CommissionCalculation;
import mz.co.mozbuy.e_ticket.event.core.enums.SaleStatus;
import mz.co.mozbuy.e_ticket.event.core.exceptions.*;
import mz.co.mozbuy.e_ticket.event.core.model.*;
import mz.co.mozbuy.e_ticket.event.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

    @Transactional
    public SaleResponseDTO createSale(CreateSaleDTO saleDTO) {
        log.info("Creating sale for ticket: {}, quantity: {}",
                saleDTO.getTicketId(), saleDTO.getQuantity());

        // 1. Buscar ticket com LOCK PESSIMISTA (evita concorrência)
        EventTicket ticket = eventTicketRepository.findByIdWithLock(saleDTO.getTicketId())
                .orElseThrow(() -> new TicketNotFoundException(saleDTO.getTicketId()));

        // 2. Validar ticket para venda (método unificado)
        validateTicketForSale(ticket, saleDTO.getQuantity());

        Event event = ticket.getEvent();
        Organizer organizer = event.getOrganizer();

        // 3. Calcular preço base
        BigDecimal unitPrice = ticket.getCurrentPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(saleDTO.getQuantity()));

        // 4. Aplicar cupom se fornecido
        DiscountCoupon coupon = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (saleDTO.getCouponCode() != null && !saleDTO.getCouponCode().trim().isEmpty()) {
            coupon = applyCoupon(saleDTO.getCouponCode(), event, subtotal);
            if (coupon != null) {
                discountAmount = coupon.applyDiscount(subtotal);
                coupon.useCoupon(); // Incrementar contador de uso
            }
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        // 5. Calcular comissão (ESTRATÉGIA HÍBRIDA)
        CommissionCalculation commission = calculateCommission(totalAmount, organizer,
                event, saleDTO.getQuantity());

        // 6. Criar registro de venda
        TicketSale sale = createSaleRecord(saleDTO, ticket, event, organizer, coupon,
                unitPrice, subtotal, discountAmount, totalAmount, commission);

        // 7. Atualizar estoque (COM LOCK GARANTIDO)
        updateTicketInventory(ticket, saleDTO.getQuantity());

        // 8. Atualizar estatísticas financeiras
        updateFinancialStats(event, totalAmount, commission.amount(), commission.payout());

        // 9. Atualizar organizador
        updateOrganizerStats(organizer, totalAmount, commission.amount(), saleDTO.getQuantity());

        // 10. Se for evento trial, marcar como usado
        if (commission.isTrial()) {
            organizer.consumeTrialEvent();
        }

        // 11. Salvar todas as alterações (ORDEM IMPORTANTE)
        // Primeiro: Salvar ticket (já atualizado)
        eventTicketRepository.save(ticket);

        // Segundo: Salvar cupom (se usado)
        if (coupon != null) {
            discountCouponRepository.save(coupon);
        }

        // Terceiro: Salvar evento (estatísticas atualizadas)
        eventRepository.save(event);

        // Quarto: Salvar organizador (saldo atualizado)
        organizerRepository.save(organizer);

        // Quinto: Salvar venda (depende dos anteriores)
        ticketSaleRepository.save(sale);

        log.info("Sale created successfully. Transaction ID: {}, Commission: {}%, Amount: {}",
                sale.getTransactionId(),
                commission.rate().multiply(BigDecimal.valueOf(100)),
                commission.amount());

        return SaleResponseDTO.fromEntity(sale);
    }

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

    // 🔥 MÉTODO IMPLEMENTADO: Validar disponibilidade do ticket
    private void validateTicketAvailability(EventTicket ticket, Integer requestedQuantity) {
        if (!ticket.getLifeCycleState().equals(LifeCycleState.ACTIVE)) {
            throw new RuntimeException("Ticket is not active");
        }

        if (!ticket.isAvailable()) {
            throw new RuntimeException("Ticket is sold out");
        }

        if (ticket.getAvailableQuantity() < requestedQuantity) {
            throw new RuntimeException(
                    String.format("Only %d tickets available, requested %d",
                            ticket.getAvailableQuantity(), requestedQuantity)
            );
        }
    }

    // 🔥 MÉTODO IMPLEMENTADO: Validar período de vendas
    private void validateSalesPeriod(EventTicket ticket) {
        if (!ticket.isSalesPeriodActive()) {
            throw new RuntimeException("Ticket sales period is not active");
        }
    }

    // 🔥 MÉTODO IMPLEMENTADO: Aplicar cupom
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

        coupon.useCoupon();
        return coupon;
    }

    // 🔥 MÉTODO IMPLEMENTADO: Calcular comissão
    private CommissionCalculation calculateCommission(BigDecimal totalAmount, Organizer organizer,
                                                      Event event, Integer quantity) {
        boolean isTrialEvent = event.isEligibleForTrial();

        if (isTrialEvent) {
            // ESTRATÉGIA: Trial - 0% de comissão
            return new CommissionCalculation(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    totalAmount,
                    true
            );
        } else {
            // ESTRATÉGIA HÍBRIDA: Taxa fixa + percentual
            BigDecimal commissionRate = event.getEffectiveCommissionRate();
            BigDecimal flatFee = event.getEffectiveFlatFee();

            // Calcular comissão da taxa fixa
            BigDecimal flatFeeCommission = flatFee.multiply(BigDecimal.valueOf(quantity));

            // Calcular comissão do percentual
            BigDecimal percentageCommission = totalAmount.multiply(commissionRate);

            // Total da comissão
            BigDecimal totalCommission = flatFeeCommission.add(percentageCommission);

            // Payout para o organizador
            BigDecimal payout = totalAmount.subtract(totalCommission);

            return new CommissionCalculation(
                    commissionRate,
                    totalCommission,
                    payout,
                    false
            );
        }
    }

    // 🔥 MÉTODO IMPLEMENTADO: Criar registro de venda
    private TicketSale createSaleRecord(CreateSaleDTO saleDTO, EventTicket ticket, Event event,
                                        Organizer organizer, DiscountCoupon coupon,
                                        BigDecimal unitPrice, BigDecimal subtotal,
                                        BigDecimal discountAmount, BigDecimal totalAmount,
                                        CommissionCalculation commission) {
        TicketSale sale = new TicketSale();
        sale.setTransactionId(generateTransactionId());
        sale.setEvent(event);
        sale.setTicket(ticket);
        sale.setOrganizer(organizer);
        sale.setDiscountCoupon(coupon);
        sale.setQuantity(saleDTO.getQuantity());
        sale.setUnitPrice(unitPrice);
        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(discountAmount);
        sale.setTotalAmount(totalAmount);
        sale.setCommissionRate(commission.rate());
        sale.setCommissionAmount(commission.amount());
        sale.setOrganizerPayout(commission.payout());
        sale.setBuyerEmail(saleDTO.getBuyerEmail());
        sale.setBuyerName(saleDTO.getBuyerName());
        sale.setBuyerPhone(saleDTO.getBuyerPhone());
        sale.setIsTrialEvent(commission.isTrial());

        // Status inicial
        sale.setStatus(SaleStatus.PENDING);

        return sale;
    }

    // 🔥 MÉTODO IMPLEMENTADO: Atualizar inventário do ticket
    private void updateTicketInventory(EventTicket ticket, Integer quantity) {
        // VALIDAÇÃO EXTRA (redundante mas segura)
        if (ticket.getAvailableQuantity() < quantity) {
            // Isso não deveria acontecer, mas é um fail-safe
            throw new IllegalStateException(
                    String.format("Concurrency issue detected! Ticket %d: available %d, requested %d",
                            ticket.getId(), ticket.getAvailableQuantity(), quantity)
            );
        }

        // ATUALIZAÇÃO ATÔMICA (dentro da transação com lock)
        int newSoldQuantity = ticket.getSoldQuantity() + quantity;
        int newAvailableQuantity = ticket.getAvailableQuantity() - quantity;

        ticket.setSoldQuantity(newSoldQuantity);
        ticket.setAvailableQuantity(newAvailableQuantity);

        // Atualizar estatísticas do evento
        Event event = ticket.getEvent();
        if (event != null) {
            event.setSoldTickets(event.getSoldTickets() + quantity);
            event.setAvailableTickets(event.getAvailableTickets() - quantity);
            event.updateTicketStatistics(); // Recalcula totais
        }
    }
    // 🔥 MÉTODO IMPLEMENTADO: Atualizar estatísticas financeiras
    private void updateFinancialStats(Event event, BigDecimal saleAmount,
                                      BigDecimal commission, BigDecimal payout) {
        // Atualizar estatísticas do evento
        event.updateFinancialStats(saleAmount, commission, payout);

        // Atualizar estatísticas de tickets do evento
        event.updateTicketStatistics();
    }

    // 🔥 MÉTODO IMPLEMENTADO: Atualizar estatísticas do organizador
    private void updateOrganizerStats(Organizer organizer, BigDecimal saleAmount,
                                      BigDecimal commission, Integer quantity) {
        if (organizer == null) return;

        // Atualizar earnings
        organizer.addEarnings(saleAmount);

        // Atualizar comissão paga (se não for trial)
        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            organizer.addCommissionPaid(commission);
        }

        // Atualizar contador de tickets vendidos
        organizer.incrementTicketsSold(quantity);
    }

    // 🔥 MÉTODO IMPLEMENTADO: Gerar ID de transação
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() +
                "-" + System.currentTimeMillis();
    }

    // 🔥 MÉTODO ADICIONAL: Processar pagamento (para integração futura)
    @Transactional
    public SaleResponseDTO processPayment(String transactionId, String paymentMethod,
                                          String paymentReference) {
        TicketSale sale = ticketSaleRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + transactionId));

        // Marcar como pago
        sale.markAsPaid(paymentMethod, paymentReference);

        // Atualizar saldo do organizador
        Organizer organizer = sale.getOrganizer();
        organizer.addToBalance(sale.getOrganizerPayout());

        // Salvar alterações
        ticketSaleRepository.save(sale);
        organizerRepository.save(organizer);

        log.info("Payment processed for transaction: {}, Amount: {}",
                transactionId, sale.getTotalAmount());

        return SaleResponseDTO.fromEntity(sale);
    }

    // 🔥 MÉTODO ADICIONAL: Cancelar venda
    @Transactional
    public SaleResponseDTO cancelSale(String transactionId, String reason) {
        TicketSale sale = ticketSaleRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + transactionId));

        // Cancelar venda
        sale.markAsCancelled();

        // Restaurar estoque
        EventTicket ticket = sale.getTicket();
        ticket.setSoldQuantity(ticket.getSoldQuantity() - sale.getQuantity());
        ticket.setAvailableQuantity(ticket.getAvailableQuantity() + sale.getQuantity());

        // Atualizar evento
        Event event = sale.getEvent();
        event.setSoldTickets(event.getSoldTickets() - sale.getQuantity());
        event.setAvailableTickets(event.getAvailableTickets() + sale.getQuantity());

        // Reverter estatísticas financeiras se já estava pago
        if (SaleStatus.PAID.equals(sale.getStatus())) {
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

        // Salvar alterações
        ticketSaleRepository.save(sale);
        eventTicketRepository.save(ticket);
        eventRepository.save(event);

        log.info("Sale cancelled: {}, Reason: {}", transactionId, reason);

        return SaleResponseDTO.fromEntity(sale);
    }


    // 🔥 MÉTODO IMPLEMENTADO: Buscar venda por transactionId
    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleByTransactionId(String transactionId) {
        log.info("Getting sale by transactionId: {}", transactionId);
        TicketSale sale = ticketSaleRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Sale not found with transactionId: " + transactionId));
        return SaleResponseDTO.fromEntity(sale);
    }
    
    // 🔥 MÉTODO IMPLEMENTADO: Buscar vendas por evento
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByEventId(Long eventId) {
        log.info("Getting sales for eventId: {}", eventId);
        List<TicketSale> sales = ticketSaleRepository.findByEventId(eventId);
        return sales.stream()
                .map(SaleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 🔥 MÉTODO IMPLEMENTADO: Buscar vendas por organizador
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByOrganizerId(Long organizerId) {
        log.info("Getting sales for organizerId: {}", organizerId);
        List<TicketSale> sales = ticketSaleRepository.findByOrganizerId(organizerId);
        return sales.stream()
                .map(SaleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 🔥 MÉTODO IMPLEMENTADO: Calcular preço com cupom (sem criar venda)
    @Transactional(readOnly = true)
    public Double calculatePriceWithCoupon(Long ticketId, Integer quantity, String couponCode) {
        log.info("Calculating price for ticket: {}, quantity: {}, couponCode: {}", ticketId, quantity, couponCode);

        // Buscar ticket
        EventTicket ticket = eventTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        Event event = ticket.getEvent();

        // Validar disponibilidade e período de vendas (mas não atualizar estoque)
        validateTicketAvailability(ticket, quantity);
        validateSalesPeriod(ticket);

        // Calcular preço base
        BigDecimal unitPrice = ticket.getCurrentPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // Aplicar cupom se fornecido
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            DiscountCoupon coupon = discountCouponRepository
                    .findByCodeAndEventId(couponCode, event.getId())
                    .orElseThrow(() -> new RuntimeException("Coupon not found: " + couponCode));

            if (!coupon.isValid()) {
                throw new RuntimeException("Coupon is not valid");
            }

            if (!coupon.canApplyToPurchase(subtotal)) {
                throw new RuntimeException("Coupon cannot be applied to this purchase amount");
            }

            discountAmount = coupon.applyDiscount(subtotal);
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        // Retornar o valor final (como Double, se for o caso)
        return totalAmount.doubleValue();
    }
}