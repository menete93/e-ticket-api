//package mz.co.mozbuy.e_ticket.event.core.service.pricing;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
//import mz.co.mozbuy.e_ticket.event.core.model.ScheduledPriceChange;
//import mz.co.mozbuy.e_ticket.event.core.repository.PricingStrategyRepository;
//import mz.co.mozbuy.e_ticket.event.core.repository.ScheduledPriceChangeRepository;
//import mz.co.mozbuy.e_ticket.event.core.service.PricingService;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class PricingStrategyScheduler  {
//
//    private final ScheduledPriceChangeRepository scheduledPriceChangeRepository;
//    private final PricingService pricingService;
//
//    @Scheduled(fixedDelay = 60000) // Executa a cada minuto
//    @Transactional
//    public void processScheduledChanges() {
//        LocalDateTime now = LocalDateTime.now();
//        List<ScheduledPriceChange> pendingChanges =
//                scheduledPriceChangeRepository.findByScheduledAtLessThanEqualAndIsExecutedFalse(now);
//
//        for (ScheduledPriceChange change : pendingChanges) {
//            try {
//                // Lógica para aplicar a mudança
//                if (change.getApplyToAllTickets()) {
//                    // Aplicar a todos os tickets do evento
//                } else if (change.getEventTicket() != null) {
//                    // Aplicar a um ticket específico
//                }
//
//                change.setIsExecuted(true);
//                change.setExecutedAt(now);
//                change.setExecutionResult("SUCCESS");
//
//            } catch (Exception e) {
//                change.setExecutionResult("ERROR: " + e.getMessage());
//                log.error("Failed to execute scheduled price change: {}", change.getId(), e);
//            }
//
//            scheduledPriceChangeRepository.save(change);
//        }
//    }
//}
