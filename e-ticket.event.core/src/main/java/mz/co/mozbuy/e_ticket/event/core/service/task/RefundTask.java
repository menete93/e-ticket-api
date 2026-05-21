//package mz.co.mozbuy.e_ticket.event.core.service.task;
//
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import mz.co.mozbuy.e_ticket.event.core.model.Event;
//import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;
//import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
//import mz.co.mozbuy.e_ticket.event.core.repository.TicketSaleRepository;
//import mz.co.mozbuy.e_ticket.event.core.service.PaymentService;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RefundTask {
//
//    private final TicketSaleRepository ticketSaleRepository;
//    private final EventRepository eventRepository;
//    private final PaymentService paymentService;
//
//    /**
//     * Processa reembolso para um evento específico (chamado pelo cancelamento)
//     */
//    @Async
//    @Transactional
//    public void processRefundsForEvent(Long eventId) {
//        log.info("🔄 [TASK] Processando reembolsos para evento ID: {}", eventId);
//
//        try {
//            Event event = eventRepository.findById(eventId)
//                    .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + eventId));
//
//            List<TicketSale> sales = ticketSaleRepository.findByEventId(eventId);
//            int refundCount = 0;
//
//            for (TicketSale sale : sales) {
//                if ("PAID".equals(sale.getStatus())) {
//                    try {
//                        boolean refundSuccess = paymentService.processRefund(sale.getTransactionId(), sale.getTotalAmount());
//
//                        if (refundSuccess) {
//                            sale.setStatus("REFUNDED");
//                            sale.setRefundedAt(LocalDateTime.now());
//                            ticketSaleRepository.save(sale);
//                            refundCount++;
//                            log.info("✅ Reembolso processado para venda ID: {}", sale.getId());
//                        } else {
//                            sale.setStatus("REFUND_FAILED");
//                            ticketSaleRepository.save(sale);
//                            log.error("❌ Falha no reembolso para venda ID: {}", sale.getId());
//                        }
//                    } catch (Exception e) {
//                        log.error("❌ Erro ao processar reembolso para venda {}: {}", sale.getId(), e.getMessage());
//                        sale.setStatus("REFUND_FAILED");
//                        ticketSaleRepository.save(sale);
//                    }
//                }
//            }
//
//            event.setRefundProcessed(true);
//            eventRepository.save(event);
//
//            log.info("✅ [TASK] Reembolsos concluídos para evento {}: {} reembolsos", eventId, refundCount);
//        } catch (Exception e) {
//            log.error("❌ [TASK] Erro ao processar reembolsos: {}", e.getMessage());
//        }
//    }
//
//    /**
//     * Task agendada para reprocessar reembolsos que falharam
//     * Executa a cada 30 minutos
//     */
//    @Scheduled(cron = "0 */30 * * * *")
//    @Transactional
//    public void reprocessFailedRefunds() {
//        log.info("🔄 [TASK] Iniciando reprocessamento de reembolsos falhos - {}", LocalDateTime.now());
//
//        List<TicketSale> failedRefunds = ticketSaleRepository.findByStatusAndRefundedAtIsNull("REFUND_FAILED");
//        int reprocessCount = 0;
//
//        for (TicketSale sale : failedRefunds) {
//            try {
//                boolean refundSuccess = paymentService.processRefund(sale.getTransactionId(), sale.getTotalAmount());
//
//                if (refundSuccess) {
//                    sale.setStatus("REFUNDED");
//                    sale.setRefundedAt(LocalDateTime.now());
//                    ticketSaleRepository.save(sale);
//                    reprocessCount++;
//                    log.info("✅ Reembolso reprocessado com sucesso para venda ID: {}", sale.getId());
//                }
//            } catch (Exception e) {
//                log.error("❌ Falha ao reprocessar reembolso para venda {}: {}", sale.getId(), e.getMessage());
//            }
//        }
//
//        log.info("✅ [TASK] Reprocessamento concluído. {} reembolsos processados", reprocessCount);
//    }
//}