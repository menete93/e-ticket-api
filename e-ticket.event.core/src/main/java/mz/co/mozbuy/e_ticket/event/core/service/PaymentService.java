package mz.co.mozbuy.e_ticket.event.core.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import mz.co.mozbuy.e_ticket.event.core.dto.PaymentRequest.PaymentRequest;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional
    public PaymentTransactionEntity initiatePayment(PaymentRequest request) {
        log.info("💰 Iniciando pagamento para venda: {}", request.getSaleId());

        // Gerar ID único da transação
        String transactionId = generateTransactionId();

        PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                .transactionId(transactionId)
                .saleId(request.getSaleId())
                .eventId(request.getEventId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .payerPhone(request.getPhoneNumber())
                .payerEmail(request.getEmail())
                .payerName(request.getName())
                .status("PENDING")
                .currency("MZN")
                .build();

        // Simular checkout ID do provedor
        transaction.setProviderCheckoutId("CHECKOUT_" + System.currentTimeMillis());

        return paymentTransactionRepository.save(transaction);
    }

    @Transactional
    public void processCallback(String providerCode, Map<String, Object> callbackData) {
        log.info("📞 Processando callback do provedor: {}", providerCode);

        String checkoutRequestId = (String) callbackData.get("CheckoutRequestID");
        Integer resultCode = (Integer) callbackData.get("ResultCode");
        String resultDesc = (String) callbackData.get("ResultDesc");

        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByProviderCheckoutId(checkoutRequestId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada: " + checkoutRequestId));

        if (resultCode == 0) {
            transaction.setStatus("SUCCESS");
            transaction.setCompletedAt(LocalDateTime.now());
            log.info("✅ Pagamento confirmado: {}", transaction.getTransactionId());
        } else {
            transaction.setStatus("FAILED");
            transaction.setProviderResultCode(resultCode);
            transaction.setProviderResultDesc(resultDesc);
            log.error("❌ Pagamento falhou: {} - {}", transaction.getTransactionId(), resultDesc);
        }

        transaction.setCallbackPayload(callbackData);
        paymentTransactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public PaymentTransactionEntity findByTransactionId(String transactionId) {
        return paymentTransactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada: " + transactionId));
    }

    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}