//package mz.co.mozbuy.e_ticket.event.core.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import mz.co.mozbuy.e_ticket.event.core.dto.*;
//import mz.co.mozbuy.e_ticket.event.core.dto.PaymentRequest.PaymentRequest;
//import mz.co.mozbuy.e_ticket.event.core.dto.PaymentResponse.PaymentResponse;
//import mz.co.mozbuy.e_ticket.event.core.mapper.PaymentTransactionMapper;
//import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
//import mz.co.mozbuy.e_ticket.event.core.service.PaymentService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/payment")
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentController {
//
//    private final PaymentService paymentService;
//    private final PaymentTransactionMapper paymentTransactionMapper;
//
//    @PostMapping("/mpesa/initiate")
//    public ResponseEntity<PaymentResponse> initiateMpesaPayment(@RequestBody MpesaPaymentRequest request) {
//        log.info("📱 Iniciando pagamento M-Pesa para venda: {}", request.getSaleId());
//
//        PaymentRequest paymentRequest = PaymentRequest.builder()
//                .saleId(request.getSaleId())
//                .eventId(request.getEventId())
//                .userId(request.getUserId())
//                .providerCode("MPESA")
//                .amount(request.getAmount())
//                .phoneNumber(request.getPhoneNumber())
//                .email(request.getPayerEmail())
//                .name(request.getPayerName())
//                .build();
//
//        PaymentTransactionEntity transaction = paymentService.initiatePayment(paymentRequest);
//
//        return ResponseEntity.ok(PaymentResponse.builder()
//                .transactionId(transaction.getTransactionId())
//                .checkoutRequestId(transaction.getProviderCheckoutId())
//                .status(transaction.getStatus())
//                .message("Pagamento iniciado com sucesso")
//                .build());
//    }
//
//    @PostMapping("/mpesa/callback")
//    public ResponseEntity<Map<String, String>> mpesaCallback(@RequestBody Map<String, Object> callbackData) {
//        log.info("📞 Recebido callback do M-Pesa");
//
//        paymentService.processCallback("MPESA", callbackData);
//
//        return ResponseEntity.ok(Map.of("ResultCode", "0", "ResultDesc", "Success"));
//    }
//
//    @GetMapping("/transaction/{transactionId}")
//    public ResponseEntity<PaymentTransactionDTO> getTransaction(@PathVariable String transactionId) {
//        PaymentTransactionEntity transaction = paymentService.findByTransactionId(transactionId);
//        PaymentTransactionDTO dto = paymentTransactionMapper.toDTO(transaction);
//        return ResponseEntity.ok(dto);
//    }
//}