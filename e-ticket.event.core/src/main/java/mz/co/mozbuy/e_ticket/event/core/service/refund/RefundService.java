//// RefundService.java
//package mz.co.mozbuy.e_ticket.event.core.service.refund;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;
//import mz.co.mozbuy.e_ticket.event.core.repository.TicketSaleRepository;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class RefundService {
//
//    private final TicketSaleRepository ticketSaleRepository;
//    private final PaymentService paymentService;
//
//    @Async
//    @Transactional
//    public void processRefundsForEvent(Long eventId) {
//        log.info("Processing refunds for event: {}", eventId);
//
//        List<TicketSale> sales = ticketSaleRepository.findByEventId(eventId);
//        int refundCount = 0;
//
//        for (TicketSale sale : sales) {
//            if ("PAID".equals(sale.getStatus())) {
//                try {
//                    paymentService.processRefund(sale.getTransactionId());
//                    sale.setStatus("REFUNDED");
//                    ticketSaleRepository.save(sale);
//                    refundCount++;
//                    log.info("Refund processed for sale: {}", sale.getId());
//                } catch (Exception e) {
//                    log.error("Failed to process refund for sale: {}", sale.getId(), e);
//                }
//            }
//        }
//
//        log.info("Refunds completed for event: {} - {} refunds processed", eventId, refundCount);
//    }
//}