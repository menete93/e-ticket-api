package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.MpesaPaymentRequest;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentResponse.PaymentResponse;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.PaymentTransactionRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.PaymentTransactionResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.TransactionStatusResponse;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import mz.co.mozbuy.e_ticket.event.core.service.payment.HybridPaymentService;
import mz.co.mozbuy.e_ticket.event.core.service.payment.PaymentResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment/mpesa")
@RequiredArgsConstructor
public class MpesaPaymentController {

    private final HybridPaymentService hybridPaymentService;


    /**
     * Iniciar pagamento M-PESA (Síncrono)
     * POST /payment/mpesa/initiate
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody MpesaPaymentRequest request) {
        log.info("📱 Iniciando pagamento M-PESA - ReservationCode: {}, Venda: {}, Telefone: {}",
                request.getReservationCode(), request.getSaleId(), request.getPhoneNumber());

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("phoneNumber", request.getPhoneNumber());
        paymentData.put("paymentMethodCode", "MPESA");

        // M-PESA é síncrono - a resposta vem imediatamente
        PaymentResult result = hybridPaymentService.processPayment(
                request.getReservationCode(),
                request.getSaleId(),
                paymentData);

        return buildResponse(result);
    }

    /**
     * Consultar status de uma reserva/transação
     * GET /payment/mpesa/status/{reservationCode}
     */
    @GetMapping("/status/{reservationCode}")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(@PathVariable String reservationCode) {
        log.info("🔍 Consultando status da reserva: {}", reservationCode);

        PaymentResult result = hybridPaymentService.getTransactionStatus(reservationCode);

        TransactionStatusResponse response = TransactionStatusResponse.builder()
                .transactionId(reservationCode)
                .status(getStatusString(result))
                .message(result.getMessage())
                .paid(result.isPaid())
                .canRetry(result.hasRetry())
                .attemptNumber(result.getAttemptNumber())
                .maxRetries(result.getMaxRetries())
                .expiresAt(result.getExpiresAt())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Nova tentativa de pagamento (após falha)
     * POST /payment/mpesa/retry/{reservationCode}
     */
    @PostMapping("/retry/{reservationCode}")
    public ResponseEntity<PaymentResponse> retryPayment(
            @PathVariable String reservationCode,
            @RequestBody Map<String, Object> paymentData) {

        log.info("🔄 Nova tentativa de pagamento M-PESA: {}", reservationCode);

        if (!paymentData.containsKey("phoneNumber")) {
            return ResponseEntity.badRequest().body(
                    PaymentResponse.builder()
                            .success(false)
                            .message("Número de telefone é obrigatório")
                            .build()
            );
        }

        paymentData.put("paymentMethodCode", "MPESA");

        PaymentResult result = hybridPaymentService.retryPayment(reservationCode, paymentData);
        return buildResponse(result);
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    private String getStatusString(PaymentResult result) {
        if (result.isSuccess()) return "SUCCESS";
        if (result.isPending()) return "PENDING";
        if (result.isExpired()) return "EXPIRED";
        return "FAILED";
    }

    private ResponseEntity<PaymentResponse> buildResponse(PaymentResult result) {
        PaymentResponse response = PaymentResponse.builder()
                .success(result.isSuccess())
                .pending(result.isPending())
                .paid(result.isPaid())
                .transactionId(result.getTransactionId())
                .providerTransactionId(result.getProviderTransactionId())
                .attemptNumber(result.getAttemptNumber())
                .maxRetries(result.getMaxRetries())
                .canRetry(result.hasRetry())
                .message(result.getMessage())
                .expiresAt(result.getExpiresAt())
                .build();

        if (result.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if (result.isPending()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
        if (result.hasRetry()) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
        }
        if (result.isExpired()) {
            return ResponseEntity.status(HttpStatus.GONE).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}