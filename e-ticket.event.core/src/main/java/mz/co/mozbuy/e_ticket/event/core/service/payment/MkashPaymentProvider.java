//package mz.co.mozbuy.e_ticket.event.core.service.payment;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.ProviderResponse;
//import mz.co.mozbuy.e_ticket.event.core.enums.FlowType;
//import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.UUID;
//
//@Slf4j
//@Component("MKASH")
//@RequiredArgsConstructor
//public class MkashPaymentProvider implements PaymentProvider {
//
//    @Override
//    public FlowType getFlowType() {
//        return FlowType.ASYNCHRONOUS;
//    }
//
//    @Override
//    public String getCode() {
//        return "MKASH";
//    }
//
//    @Override
//    public ProviderResponse processPayment(TicketSale sale, Map<String, Object> paymentData) {
//        log.info("📱 Iniciando pagamento M-Kash (assíncrono) - Venda: {}", sale.getTransactionId());
//
//        String transactionRef = "MK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
//        String qrCodeUrl = "https://api.mkash.com/qr/" + transactionRef;
//        String paymentUrl = "https://pay.mkash.com/" + transactionRef;
//
//        return ProviderResponse.pending(
//                        "MKASH_QR_CODE",
//                        paymentUrl,
//                        LocalDateTime.now().plusMinutes(60),
//                        "1. Abra o aplicativo M-Kash\n2. Leia o QR Code abaixo\n3. Confirme o pagamento\n4. O sistema confirmará automaticamente"
//                ).withQrCodeUrl(qrCodeUrl)
//                .withRawResponse(Map.of("transactionRef", transactionRef, "status", "PENDING"));
//    }
//
//    @Override
//    public ProviderResponse processCallback(String transactionId, Map<String, Object> callbackData) {
//        log.info("📞 Callback M-Kash recebido - Transação: {}", transactionId);
//
//        String status = (String) callbackData.get("status");
//        String transactionRef = (String) callbackData.get("transactionRef");
//
//        if ("SUCCESS".equals(status) || "CONFIRMED".equals(status)) {
//            return ProviderResponse.success(transactionRef, "SUCCESS", "Pagamento confirmado");
//        } else if ("FAILED".equals(status)) {
//            return ProviderResponse.failed("Pagamento falhou", "PROVIDER_ERROR", false);
//        }
//
//        return ProviderResponse.failed("Status desconhecido", "UNKNOWN", false);
//    }
//
//    @Override
//    public ProviderResponse checkStatus(String transactionId) {
//        return ProviderResponse.failed("Status check via polling não implementado", "NOT_IMPLEMENTED", false);
//    }
//
//    @Override
//    public boolean validateCallbackSignature(Map<String, String> headers, String payload, String signature) {
//        // Validar assinatura conforme documentação do M-Kash
//        return true;
//    }
//}