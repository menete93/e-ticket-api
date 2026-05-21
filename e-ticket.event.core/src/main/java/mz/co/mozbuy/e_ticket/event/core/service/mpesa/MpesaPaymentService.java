package mz.co.mozbuy.e_ticket.event.core.service.mpesa;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.MpesaResultDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.PaymentTransactionRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.exceptions.BusinessException;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentMethodEntity;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentProviderConfigEntity;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.PaymentMethodRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.PaymentProviderConfigRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaPaymentService {

    private final MpesaIntegrationService mpesaIntegrationService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentProviderConfigRepository paymentProviderConfigRepository;
    private final PaymentMethodRepository paymentMethodRepository;  // ← ADICIONAR


    /**
     * Processa pagamento via M-Pesa
     */
    @Transactional
    public MpesaResultDTO processMpesaPayment(String transactionId, String phoneNumber) {
        log.info("💰 Processando pagamento M-Pesa - Transação: {}", transactionId);

        // 1. Buscar transação
        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND",
                        "Transação não encontrada: " + transactionId));

        // 2. Validar status da transação
        if (transaction.isSuccess()) {
            throw new BusinessException("TRANSACTION_ALREADY_PAID",
                    "Transação já foi paga: " + transactionId);
        }

        if (transaction.isFailed()) {
            throw new BusinessException("TRANSACTION_FAILED",
                    "Transação já falhou: " + transactionId);
        }

        // 3. Buscar configuração do provedor M-Pesa
        PaymentProviderConfigEntity providerConfig = paymentProviderConfigRepository
                .findByCode("MPESA")
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND",
                        "Configuração do M-Pesa não encontrada"));

        // 4. Validar se provedor está ativo
        if (!providerConfig.isActive()) {
            throw new BusinessException("PROVIDER_INACTIVE",
                    "Provedor M-Pesa inativo");
        }

        // 5. Atualizar status da transação
        transaction.setStatus("PROCESSING");
        transaction.setProcessedAt(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        // 6. Chamar API do M-Pesa
        MpesaResultDTO result = mpesaIntegrationService.sendToMPesa(
                phoneNumber,
                transaction,
                providerConfig
        );

        // 7. Processar resposta
        if (result.isSuccessful() && result.getResponseCode().equals("Ins-0")) {
            transaction.setStatus("SUCCESS");
            transaction.setProviderCheckoutId(result.getTransactionID());
            transaction.setProviderResultCode(Integer.parseInt(result.getResponseCode()));
            transaction.setProviderResultDesc(result.getResponseDescription());
            transaction.setCompletedAt(LocalDateTime.now());

            log.info("✅ Pagamento M-Pesa realizado com sucesso - Transação: {}, ID: {}",
                    transaction.getTransactionId(), result.getTransactionID());
        } else {
            transaction.setStatus("FAILED");
            transaction.setProviderResultCode(result.getStatusCode());
            transaction.setProviderResultDesc(result.getResponseDescription());

            log.error("❌ Pagamento M-Pesa falhou - Transação: {}, Motivo: {}",
                    transaction.getTransactionId(), result.getResponseDescription());
        }

        paymentTransactionRepository.save(transaction);

        return result;
    }

    /**
     * Processa callback do M-Pesa
     */
    @Transactional
    public void processMpesaCallback(String transactionId, MpesaResultDTO callbackData) {
        log.info("📞 Processando callback M-Pesa - Transação: {}", transactionId);

        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND",
                        "Transação não encontrada: " + transactionId));

        if (callbackData.isSuccessResponse()) {
            transaction.setStatus("SUCCESS");
            transaction.setProviderCheckoutId(callbackData.getTransactionID());
            transaction.setProviderResultCode(Integer.parseInt(callbackData.getResponseCode()));
            transaction.setProviderResultDesc(callbackData.getResponseDescription());
            transaction.setCompletedAt(LocalDateTime.now());

            log.info("✅ Callback M-Pesa confirmado - Transação: {}", transactionId);
        } else {
            transaction.setStatus("FAILED");
            transaction.setProviderResultCode(Integer.parseInt(callbackData.getResponseCode()));
            transaction.setProviderResultDesc(callbackData.getResponseDescription());

            log.error("❌ Callback M-Pesa falhou - Transação: {}, Motivo: {}",
                    transactionId, callbackData.getResponseDescription());
        }

//        transaction.setCallbackPayload(convertToMap(callbackData));
        paymentTransactionRepository.save(transaction);
    }

    /**
     * Consulta status da transação
     */
    public MpesaResultDTO getTransactionStatus(String transactionId) {
        log.info("🔍 Consultando status da transação M-Pesa: {}", transactionId);

        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND",
                        "Transação não encontrada: " + transactionId));

        // Buscar configuração do provedor
        PaymentProviderConfigEntity providerConfig = paymentProviderConfigRepository
                .findByCode("MPESA")
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND",
                        "Configuração do M-Pesa não encontrada"));

        // Consultar status na API do M-Pesa
        MpesaResultDTO result = mpesaIntegrationService.queryTransactionStatus(transactionId, providerConfig);

        // Atualizar status local se necessário
        if (result.isSuccessful() && result.isSuccessResponse()) {
            if (!transaction.isSuccess()) {
                transaction.setStatus("SUCCESS");
                transaction.setCompletedAt(LocalDateTime.now());
                paymentTransactionRepository.save(transaction);
            }
        }

        return result;
    }

    private java.util.Map<String, Object> convertToMap(MpesaResultDTO dto) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("responseCode", dto.getResponseCode());
        map.put("responseDescription", dto.getResponseDescription());
        map.put("transactionID", dto.getTransactionID());
        map.put("conversationID", dto.getConversationID());
        map.put("thirdPartyReference", dto.getThirdPartyReference());
        map.put("statusCode", dto.getStatusCode());
        return map;
    }

    // PaymentService.java - Adicione este método

    @Transactional
    public PaymentTransactionEntity initiatePayment(PaymentTransactionRequestDTO request) {
        log.info("💰 Iniciando transação de pagamento para venda: {}", request.getSaleId());

        // Gerar transactionId único
//        String transactionId = "TXN_" + System.currentTimeMillis() + "_" +
//                UUID.randomUUID().toString().substring(0, 8);


        String transactionId = String.format("%011d",
                Math.abs(new Random().nextLong()) % 100000000000L);

        // Buscar método de pagamento padrão (M-Pesa)
        PaymentMethodEntity method = paymentMethodRepository.findByCode("MPESA")
                .orElseThrow(() -> new RuntimeException("Método de pagamento MPESA não encontrado"));

        PaymentProviderConfigEntity provider = method.getProvider();

        PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                .transactionId(transactionId)
                .saleId(request.getSaleId())
                .eventId(request.getEventId())
                .userId(request.getUserId())
                .method(method)
                .provider(provider)
                .amount(request.getAmount())
                .payerName(request.getPayerName())
                .payerEmail(request.getPayerEmail())
                .payerPhone(request.getPayerPhone())
                .status("PENDING")
                .currency("MZN")
                .feeAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .retryCount(0)
                .maxRetries(3)
                .build();

        PaymentTransactionEntity saved = paymentTransactionRepository.save(transaction);
        return paymentTransactionRepository.findByTransactionIdWithRelations(saved.getTransactionId())
                .orElse(saved);
    }
}