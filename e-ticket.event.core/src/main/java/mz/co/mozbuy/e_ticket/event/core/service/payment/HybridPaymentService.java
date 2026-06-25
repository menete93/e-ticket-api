package mz.co.mozbuy.e_ticket.event.core.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.PaymentTransactionRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.ProviderResponse;
import mz.co.mozbuy.e_ticket.event.core.model.*;
import mz.co.mozbuy.e_ticket.event.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HybridPaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final TicketSaleRepository ticketSaleRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final EventRepository eventRepository;
    private final UserEventReservationControlRepository controlRepository;  // ✅ usando a tabela existente
    private final Map<String, PaymentProvider> paymentProviders;

    // ==================== CREATE PAYMENT TRANSACTION ====================

    @Transactional
    public PaymentTransactionEntity createPaymentTransaction(PaymentTransactionRequestDTO request) {
        log.info("💰 Criando transação de pagamento - Venda: {}, Método: {}, ReservationCode: {}",
                request.getSaleId(), request.getPaymentMethodCode(), request.getReservationCode());

        TicketSale sale = ticketSaleRepository.findById(request.getSaleId())
                .orElseThrow(() -> new RuntimeException("Venda não encontrada: " + request.getSaleId()));

        PaymentMethodEntity paymentMethod = paymentMethodRepository
                .findByCode(request.getPaymentMethodCode())
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado: " + request.getPaymentMethodCode()));

        Event event = eventRepository.findById(sale.getEvent().getId())
                .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + sale.getEvent().getId()));

        // Validar limite de reservas do usuário (se necessário)
        if (request.getUserId() != null) {
            UserEventReservationControlEntity control = getOrCreateControl(request.getUserId(), event.getId());

            if (!control.canReserve(request.getQuantity() != null ? request.getQuantity() : sale.getQuantity())) {
                String reason = control.getIsBlocked() ? control.getBlockReason() :
                        "Você excedeu o limite máximo de ingressos para este evento";
                throw new RuntimeException(reason);
            }
        }

        String reservationCode = request.getReservationCode();
        if (reservationCode == null || reservationCode.isEmpty()) {
            throw new RuntimeException("Reservation code é obrigatório e deve ser gerado previamente");
        }

        int timeoutMinutes = calculateTimeoutMinutes( request.getPaymentMethodCode());
        Integer quantity = request.getQuantity() != null ? request.getQuantity() : sale.getQuantity();

        // ✅ SEM ticketId
        PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                .reservationCode(reservationCode)
                .saleId(request.getSaleId())
                .eventId(sale.getEvent().getId())
                .userId(sale.getUserId())
                .quantity(quantity)  // ← usar a variável
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "MZN")
                .paymentMethodCode(paymentMethod.getCode())
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes))
                .build();

        PaymentTransactionEntity savedTransaction = paymentTransactionRepository.save(transaction);

        // Registrar reserva ativa no controle
        if (request.getUserId() != null) {
            UserEventReservationControlEntity control = getOrCreateControl(request.getUserId(), event.getId());
            control.registerActiveReservation();
            controlRepository.save(control);
        }

        return savedTransaction;
    }

    // ==================== PROCESS PAYMENT ====================

    @Transactional
    public PaymentResult processPayment(String reservationCode, Long saleId, Map<String, Object> paymentData) {
        log.info("💰 Processando pagamento - Reserva: {}, Venda: {}", reservationCode, saleId);

        // 1. Buscar transação
        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByReservationCode(reservationCode)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada: " + reservationCode));

        Event event = eventRepository.findById(transaction.getEventId())
                .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + transaction.getEventId()));

        // ✅ Buscar controle do usuário
        UserEventReservationControlEntity control = null;
        if (transaction.getUserId() != null) {
            control = getOrCreateControl(transaction.getUserId(), event.getId());

            if (control.getIsBlocked()) {
                return PaymentResult.blocked("Usuário bloqueado: " + control.getBlockReason());
            }
        }

        // Validar status
        if (!transaction.isPending()) {
            return PaymentResult.failed("Reserva não está pendente. Status: " + transaction.getStatus());
        }

        if (transaction.getExpiresAt().isBefore(LocalDateTime.now())) {
            transaction.markExpired();
            paymentTransactionRepository.save(transaction);

            if (control != null) {
                control.releaseActiveReservation();
                controlRepository.save(control);
            }

            return PaymentResult.failed("Reserva expirada");
        }

        TicketSale sale = ticketSaleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada: " + saleId));

        String paymentMethodCode = (String) paymentData.getOrDefault("paymentMethodCode", "MPESA");
        String phoneNumber = (String) paymentData.get("phoneNumber");

        // Contar tentativas
        long attemptCount = paymentAttemptRepository.countByPaymentTransactionId(transaction.getId());
        int attemptNumber = (int) attemptCount + 1;
        int maxAttempts = 3;

        // Registrar tentativa
        PaymentAttemptEntity attempt = PaymentAttemptEntity.builder()
                .paymentTransactionId(transaction.getId())
                .attemptNumber(attemptNumber)
                .paymentMethodCode(paymentMethodCode)
                .phoneNumber(phoneNumber)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            PaymentProvider provider = paymentProviders.get(paymentMethodCode);
            if (provider == null) {
                throw new RuntimeException("Provedor não encontrado: " + paymentMethodCode);
            }

            ProviderResponse providerResponse = provider.processPayment(sale, paymentData);
            attempt.setDurationMs(System.currentTimeMillis() - startTime);

            if (providerResponse.isSuccess()) {
                // SUCESSO
                attempt.markSuccess(providerResponse.getProviderTransactionId());
                paymentAttemptRepository.save(attempt);

                transaction.markSuccess();
                transaction.setConfirmedAt(LocalDateTime.now());
                paymentTransactionRepository.save(transaction);

                sale.markAsPaid(paymentMethodCode, providerResponse.getProviderTransactionId());
                ticketSaleRepository.save(sale);

                // ✅ Registrar compra concluída
                if (control != null) {
                    control.registerCompletedPurchase();
                    controlRepository.save(control);
                }

                return PaymentResult.successWithResponse(reservationCode, providerResponse);

            } else if (providerResponse.isPending()) {
                // PENDENTE
                attempt.setStatus("PENDING");
                paymentAttemptRepository.save(attempt);

                return PaymentResult.pending(reservationCode,
                        providerResponse.getPaymentUrl(),
                        providerResponse.getQrCodeUrl(),
                        providerResponse.getInstructions(),
                        providerResponse.getExpiresAt());

            } else {
                // FALHA
                attempt.markFailed(providerResponse.getErrorMessage());
                paymentAttemptRepository.save(attempt);

                // ✅ Registrar tentativa falha
                if (control != null) {
                    control.registerFailedAttempt();

                    if (control.getFailedAttempts() >= 3 && control.getCompletedPurchases() == 0) {
                        control.block("Múltiplas tentativas de pagamento sem sucesso");
                    }
                    controlRepository.save(control);
                }

                boolean canRetry = attemptNumber < maxAttempts &&
                        !transaction.getExpiresAt().isBefore(LocalDateTime.now());

                return PaymentResult.failed(providerResponse.getErrorMessage(), "PAYMENT_ERROR",
                        attemptNumber, maxAttempts, canRetry, transaction.getExpiresAt());
            }

        } catch (Exception e) {
            log.error("Erro ao processar pagamento", e);
            attempt.markFailed(e.getMessage());
            attempt.setDurationMs(System.currentTimeMillis() - startTime);
            paymentAttemptRepository.save(attempt);

            if (control != null) {
                control.registerFailedAttempt();
                if (control.getFailedAttempts() >= 3 && control.getCompletedPurchases() == 0) {
                    control.block("Erro sistemático repetido");
                }
                controlRepository.save(control);
            }

            boolean canRetry = attemptNumber < maxAttempts &&
                    !transaction.getExpiresAt().isBefore(LocalDateTime.now());

            return PaymentResult.failed("Erro no processamento: " + e.getMessage(), "SYSTEM_ERROR",
                    attemptNumber, maxAttempts, canRetry, transaction.getExpiresAt());
        }
    }

    // ==================== RETRY PAYMENT ====================

    @Transactional
    public PaymentResult retryPayment(String reservationCode, Map<String, Object> paymentData) {
        log.info("🔄 Nova tentativa de pagamento: {}", reservationCode);

        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByReservationCode(reservationCode)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada: " + reservationCode));

        if (!transaction.isPending()) {
            return PaymentResult.failed("Reserva não está pendente. Status: " + transaction.getStatus());
        }

        if (transaction.getExpiresAt().isBefore(LocalDateTime.now())) {
            transaction.markExpired();
            paymentTransactionRepository.save(transaction);

            if (transaction.getUserId() != null) {
                UserEventReservationControlEntity control = getOrCreateControl(
                        transaction.getUserId(), transaction.getEventId());
                control.releaseActiveReservation();
                controlRepository.save(control);
            }

            return PaymentResult.failed("Reserva expirada");
        }

        return processPayment(reservationCode, transaction.getSaleId(), paymentData);
    }

    // ==================== GET STATUS ====================

    @Transactional(readOnly = true)
    public PaymentResult getTransactionStatus(String reservationCode) {
        return paymentTransactionRepository.findByReservationCode(reservationCode)
                .map(transaction -> {
                    if (transaction.isSuccess()) {
                        return PaymentResult.success(reservationCode);
                    } else if (transaction.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return PaymentResult.failed("Reserva expirada");
                    } else {
                        long attemptCount = paymentAttemptRepository.countByPaymentTransactionId(transaction.getId());
                        return PaymentResult.failed("Pagamento pendente", "PENDING",
                                (int) attemptCount, 3, true, transaction.getExpiresAt());
                    }
                })
                .orElse(PaymentResult.failed("Reserva não encontrada: " + reservationCode));
    }

    // ==================== PROCESS CALLBACK ====================

    @Transactional
    public PaymentResult processCallback(String reservationCode, Map<String, Object> callbackData,
                                         Map<String, String> headers, String signature) {
        log.info("📞 Processando callback para reserva: {}", reservationCode);

        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByReservationCode(reservationCode)
                .orElse(null);

        if (transaction == null) {
            return PaymentResult.failed("Reserva não encontrada: " + reservationCode);
        }

        if ("SUCCESS".equals(transaction.getStatus()) || "FAILED".equals(transaction.getStatus())) {
            return PaymentResult.success(reservationCode);
        }

        String paymentMethodCode = transaction.getPaymentMethodCode();
        PaymentProvider provider = paymentProviders.get(paymentMethodCode);

        if (provider == null) {
            return PaymentResult.failed("Provedor não encontrado: " + paymentMethodCode);
        }

        ProviderResponse providerResponse = provider.processCallback(reservationCode, callbackData);

        if (providerResponse.isSuccess()) {
            transaction.markSuccess();
            transaction.setConfirmedAt(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);

            paymentAttemptRepository.findFirstByPaymentTransactionIdOrderByAttemptNumberDesc(transaction.getId())
                    .ifPresent(attempt -> {
                        attempt.markSuccess(providerResponse.getProviderTransactionId());
                        paymentAttemptRepository.save(attempt);
                    });

            if (transaction.getUserId() != null) {
                UserEventReservationControlEntity control = getOrCreateControl(
                        transaction.getUserId(), transaction.getEventId());
                control.registerCompletedPurchase();
                controlRepository.save(control);
            }

            return PaymentResult.successWithResponse(reservationCode, providerResponse);
        } else {
            return PaymentResult.failed(providerResponse.getErrorMessage());
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private UserEventReservationControlEntity getOrCreateControl(Long userId, Long eventId) {
        return controlRepository.findByUserIdAndEventIdWithLock(userId, eventId)
                .orElseGet(() -> {
                    UserEventReservationControlEntity control = UserEventReservationControlEntity.builder()
                            .userId(userId)
                            .eventId(eventId)
                            .totalAttempts(0)
                            .activeReservations(0)
                            .completedPurchases(0)
                            .failedAttempts(0)
                            .isBlocked(false)
                            .build();
                    return controlRepository.save(control);
                });
    }

    private int calculateTimeoutMinutes(String paymentMethodCode) {
        if ("CARD".equals(paymentMethodCode) || "VISA".equals(paymentMethodCode)) {
            return 60;
        }
        return 30;
    }
}