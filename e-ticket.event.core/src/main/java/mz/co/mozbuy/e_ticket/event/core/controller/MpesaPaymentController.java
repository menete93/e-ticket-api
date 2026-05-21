package mz.co.mozbuy.e_ticket.event.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.MpesaPaymentRequest;
import mz.co.mozbuy.e_ticket.event.core.dto.MpesaResultDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.PaymentTransactionRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.PaymentTransactionResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import mz.co.mozbuy.e_ticket.event.core.service.mpesa.MpesaPaymentService;
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

    private final MpesaPaymentService mpesaPaymentService;

    /**
     * Inicia pagamento M-Pesa
     */
    @PostMapping("/initiate")
    public ResponseEntity<MpesaResultDTO> initiatePayment(@Valid @RequestBody MpesaPaymentRequest request) {
        log.info("📱 Iniciando pagamento M-Pesa para venda: {}", request.getSaleId());

        MpesaResultDTO result = mpesaPaymentService.processMpesaPayment(
                request.getTransactionId(),
                request.getPhoneNumber()
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Callback do M-Pesa
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> mpesaCallback(@RequestBody Map<String, Object> callbackData) {
        log.info("📞 Recebido callback do M-Pesa");

        // ✅ CORRIGIDO - variáveis com nomes diferentes
        String transactionReference = (String) callbackData.get("TransactionReference");
        String resultCode = (String) callbackData.get("ResultCode");
        String resultDesc = (String) callbackData.get("ResultDesc");
        String transactionID = (String) callbackData.get("TransactionID");

        log.info("Callback recebido - Ref: {}, Code: {}, Desc: {}, ID: {}",
                transactionReference, resultCode, resultDesc, transactionID);

        // Criar DTO com os dados do callback
        MpesaResultDTO result = MpesaResultDTO.builder()
                .responseCode(resultCode)
                .responseDescription(resultDesc)
                .transactionID(transactionID)
                .thirdPartyReference(transactionReference)
                .build();

        // Processar callback usando o transactionReference
        mpesaPaymentService.processMpesaCallback(transactionReference, result);

        Map<String, String> response = new HashMap<>();
        response.put("ResultCode", "0");
        response.put("ResultDesc", "Success");

        return ResponseEntity.ok(response);
    }

    /**
     * Consulta status da transação
     */
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<MpesaResultDTO> getTransactionStatus(@PathVariable String transactionId) {
        log.info("🔍 Consultando status da transação M-Pesa: {}", transactionId);

        MpesaResultDTO result = mpesaPaymentService.getTransactionStatus(transactionId);

        return ResponseEntity.ok(result);
    }


    // PaymentController.java - Adicione este método

    @PostMapping("/transaction/create")
    public ResponseEntity<PaymentTransactionResponseDTO> createPaymentTransaction(
            @Valid @RequestBody PaymentTransactionRequestDTO request) {

        log.info("💰 Criando transação de pagamento - Venda: {}, Valor: {}",
                request.getSaleId(), request.getAmount());

        PaymentTransactionEntity transaction = mpesaPaymentService.initiatePayment(request);

        PaymentTransactionResponseDTO response = PaymentTransactionResponseDTO.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .saleId(transaction.getSaleId())
                .eventId(transaction.getEventId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .payerPhone(transaction.getPayerPhone())
                .payerEmail(transaction.getPayerEmail())
                .payerName(transaction.getPayerName())
                .createdAt(transaction.getCreatedAt())
                .eventName(transaction.getEventName())
                .ticketName(transaction.getTicketName())
                .quantity(transaction.getQuantity())
                .totalAmount(transaction.getAmount())
                .build();

        // 🔥 CONVERTER PARA DTO USANDO O MÉTODO ESTÁTICO

        return ResponseEntity.status(HttpStatus.CREATED).body(response);    }


}