package mz.co.mozbuy.e_ticket.event.core.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.ProviderResponse;
import mz.co.mozbuy.e_ticket.event.core.enums.FlowType;
import mz.co.mozbuy.e_ticket.event.core.integ.dto.MpesaResultDTO;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentProviderConfigEntity;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;
import mz.co.mozbuy.e_ticket.event.core.repository.PaymentProviderConfigRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.PaymentTransactionRepository;
import mz.co.mozbuy.e_ticket.event.core.service.mpesa.MpesaIntegrationService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("MPESA")
@RequiredArgsConstructor
public class MpesaPaymentProvider implements PaymentProvider {

    private final MpesaIntegrationService mpesaIntegrationService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentProviderConfigRepository providerConfigRepository;

    @Override
    public FlowType getFlowType() {
        return FlowType.SYNCHRONOUS;
    }

    @Override
    public String getCode() {
        return "MPESA";
    }

    @Override
    public ProviderResponse processPayment(TicketSale sale, Map<String, Object> paymentData) {
        String phoneNumber = (String) paymentData.get("phoneNumber");

        try {
            PaymentTransactionEntity transaction = paymentTransactionRepository
                    .findByReservationCode(sale.getTransactionId())
                    .orElseGet(() -> createTransaction(sale));

            PaymentProviderConfigEntity providerConfig = providerConfigRepository
                    .findByCode("MPESA")
                    .orElseThrow(() -> new RuntimeException("Configuração M-Pesa não encontrada"));

            transaction.setStatus("PROCESSING");
            paymentTransactionRepository.save(transaction);

            MpesaResultDTO result = mpesaIntegrationService.sendToMPesa(phoneNumber, transaction, providerConfig);

            if (result.isSuccessful() && "INS-0".equals(result.getResponseCode())) {
                transaction.setStatus("SUCCESS");
                paymentTransactionRepository.save(transaction);

                // ✅ Agora funciona!
                return ProviderResponse.success(
                        result.getTransactionID(),
                        result.getResponseCode(),
                        result.getResponseDescription(),
                        Map.of(
                                "responseCode", result.getResponseCode(),
                                "transactionId", result.getTransactionID(),
                                "conversationId", result.getConversationID()
                        )
                );
            } else {
                transaction.setStatus("FAILED");
                paymentTransactionRepository.save(transaction);

                // ✅ Agora funciona!
                return ProviderResponse.failed(
                        result.getResponseDescription(),
                        categorizeFailure(result),
                        isRetryable(result),
                        Map.of("responseCode", result.getResponseCode())
                );
            }
        } catch (Exception e) {
            log.error("Erro no M-Pesa", e);
            return ProviderResponse.failed(e.getMessage(), "PROVIDER_ERROR", true);
        }
    }
    @Override
    public ProviderResponse processCallback(String rer, Map<String, Object> callbackData) {
        return ProviderResponse.failed("M-Pesa não suporta callback", "NOT_SUPPORTED", false);
    }

    @Override
    public ProviderResponse checkStatus(String reservationCode) {
        return paymentTransactionRepository.findByReservationCode(reservationCode)
                .map(t -> "SUCCESS".equals(t.getStatus()) ?
                        ProviderResponse.success(null, null, null) :
                        ProviderResponse.failed(t.getStatus(), "TRANSACTION_FAILED", false))
                .orElse(ProviderResponse.failed("Transação não encontrada", "NOT_FOUND", false));
    }

    @Override
    public boolean validateCallbackSignature(Map<String, String> headers, String payload, String signature) {
        return true;
    }

    private PaymentTransactionEntity createTransaction(TicketSale sale) {
        PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                .reservationCode(sale.getTransactionId())
                .saleId(sale.getId())
                .eventId(sale.getEvent().getId())
                .userId(sale.getUserId())
                .paymentMethodCode("MPESA")
                .amount(sale.getTotalAmount())
                .currency("MZN")
                .status("PENDING")
                .build();
        return paymentTransactionRepository.save(transaction);
    }

    private String categorizeFailure(MpesaResultDTO result) {
        String code = result.getResponseCode();
        if (code == null) return "PROVIDER_ERROR";
        switch (code) {
            case "INS-0": return "SUCCESS";
            case "INS-1": return "INSUFFICIENT_FUNDS";
            case "INS-2": return "TIMEOUT";
            case "INS-3": return "CANCELLED";
            case "INS-5": return "INVALID_PHONE";
            default: return "PROVIDER_ERROR";
        }
    }

    private boolean isRetryable(MpesaResultDTO result) {
        String code = result.getResponseCode();
        if (code == null) return true;
        switch (code) {
            case "INS-1":
            case "INS-3":
            case "INS-5":
                return false;
            default:
                return true;
        }
    }
}