package mz.co.mozbuy.e_ticket.event.core.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.service.payment.HybridPaymentService;
import mz.co.mozbuy.e_ticket.event.core.service.payment.PaymentResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment/callback")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final HybridPaymentService hybridPaymentService;

    @PostMapping("/{providerCode}")
    public ResponseEntity<Map<String, String>> handleCallback(
            @PathVariable String providerCode,
            @RequestBody Map<String, Object> callbackData,
            @RequestHeader Map<String, String> headers) {

        log.info("📞 Callback recebido do provedor: {}", providerCode);

        String transactionId = extractTransactionId(callbackData);
        String signature = (String) callbackData.get("signature");

        PaymentResult result = hybridPaymentService.processCallback(
                transactionId, callbackData, headers, signature);

        Map<String, String> response = new HashMap<>();
        response.put("status", result.isSuccess() ? "OK" : "ERROR");
        response.put("message", result.getMessage());

        return ResponseEntity.ok(response);
    }

    private String extractTransactionId(Map<String, Object> callbackData) {
        if (callbackData.containsKey("transactionId")) {
            return (String) callbackData.get("transactionId");
        }
        if (callbackData.containsKey("TransactionReference")) {
            return (String) callbackData.get("TransactionReference");
        }
        if (callbackData.containsKey("reference")) {
            return (String) callbackData.get("reference");
        }
        return null;
    }
}