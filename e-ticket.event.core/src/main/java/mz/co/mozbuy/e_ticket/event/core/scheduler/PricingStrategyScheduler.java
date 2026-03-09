package mz.co.mozbuy.e_ticket.event.core.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.StrategyApplicationResultDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.service.PricingStrategyService;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PricingStrategyScheduler {

    private final PricingStrategyService strategyService;
    private final EventRepository eventRepository;

    // Executa a cada hora
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void applyScheduledStrategies() {
        log.info("Running scheduled pricing strategies check at {}", LocalDateTime.now());

        // Buscar eventos ativos que estão acontecendo agora ou no futuro
        List<Event> activeEvents = eventRepository.findByEventDateAfter(LocalDateTime.now());

        for (Event event : activeEvents) {
            try {
                List<StrategyApplicationResultDTO> results =
                        strategyService.applyAutoStrategiesToEvent(event.getId());

                log.info("Applied {} strategies to event: {}", results.size(), event.getName());
            } catch (Exception e) {
                log.error("Error applying strategies to event {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    // Executa a cada 15 minutos para verificar estratégias baseadas em demanda
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void checkDemandBasedStrategies() {
        log.info("Checking demand-based strategies at {}", LocalDateTime.now());

        List<Event> activeEvents = eventRepository.findByEventDateAfter(LocalDateTime.now());

        for (Event event : activeEvents) {
            try {
                strategyService.checkAndApplyDemandBasedStrategies(event.getId());
            } catch (Exception e) {
                log.error("Error checking demand strategies for event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}