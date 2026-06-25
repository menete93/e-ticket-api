package mz.co.mozbuy.e_ticket.event.core.service.payment;

import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.ProviderResponse;
import mz.co.mozbuy.e_ticket.event.core.enums.FlowType;
import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;

import java.util.Map;

public interface PaymentProvider {
    FlowType getFlowType();
    String getCode();
    ProviderResponse processPayment(TicketSale sale, Map<String, Object> paymentData);
    ProviderResponse processCallback(String transactionId, Map<String, Object> callbackData);
    ProviderResponse checkStatus(String transactionId);
    boolean validateCallbackSignature(Map<String, String> headers, String payload, String signature);
}