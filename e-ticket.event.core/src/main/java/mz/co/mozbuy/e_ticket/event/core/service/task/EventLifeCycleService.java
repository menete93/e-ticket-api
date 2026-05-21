package mz.co.mozbuy.e_ticket.event.core.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventLifeCycleService {

    private final EventRepository eventRepository;

    /**
     * Task principal - Executa a cada hora para atualizar estados dos eventos
     * Regras:
     * 1. Eventos com isCancelled = true -> state = BANNED
     * 2. Eventos com data expirada (eventDate < now) -> state = INACTIVE
     *
     * Cron: 0 0 * * * * (todo dia, a cada hora, no minuto 0)
     */
    @Scheduled(cron = "0 59 0 * * *")
    @Transactional
    public void updateEventLifeCycleStates() {
        log.info("🔄 [TASK] Iniciando atualização do ciclo de vida dos eventos - {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = 0;

        // ==================== REGRA 1: Eventos cancelados -> BANNED ====================
        List<Event> cancelledEvents = eventRepository.findByIsCancelledTrueAndStateNot(LifeCycleState.BANNED);
        for (Event event : cancelledEvents) {
            LifeCycleState oldState = event.getState();
            event.setState(LifeCycleState.BANNED);
            updatedCount++;
            log.info("🚫 Evento [{}] - {} cancelado (isCancelled=true) -> BANNED (estava: {})",
                    event.getId(), event.getName(), oldState);
        }

        // ==================== REGRA 2: Eventos com data expirada -> INACTIVE ====================
        List<Event> expiredEvents = eventRepository.findByEventDateBeforeAndIsCancelledFalse(now);
        for (Event event : expiredEvents) {
            // Ignorar eventos já cancelados (prioridade do cancelado é maior)
            if (Boolean.FALSE.equals(event.getIsCancelled())) {
                LifeCycleState oldState = event.getState();
                event.setState(LifeCycleState.INACTIVE);
                updatedCount++;
                log.info("📅 Evento [{}] - {} data expirada: {} -> INACTIVE (estava: {})",
                        event.getId(), event.getName(), event.getEventDate(), oldState);
            }
        }

        log.info("✅ [TASK] Atualização concluída. Total atualizado: {} eventos", updatedCount);
    }

    /**
     * Task para execução mais frequente (a cada 30 minutos) para casos críticos
     */
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void quickStateUpdate() {
        log.debug("🔄 [TASK-RAPIDA] Verificação rápida de estados - {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = 0;

        // Verificar apenas eventos com data que expira hoje
        List<Event> expiringTodayEvents = eventRepository.findByEventDateBetweenAndIsCancelledFalse(
                now.withHour(0).withMinute(0).withSecond(0),
                now.withHour(23).withMinute(59).withSecond(59)
        );

        for (Event event : expiringTodayEvents) {
            event.setState(LifeCycleState.INACTIVE);
            updatedCount++;
            log.info("📅 Evento [{}] - {} expirou hoje -> INACTIVE", event.getId(), event.getName());
        }

        if (updatedCount > 0) {
            log.info("✅ [TASK-RAPIDA] {} evento(s) atualizado(s)", updatedCount);
        }
    }

    /**
     * Método manual para atualizar estado de um evento específico
     */
    @Transactional
    public LifeCycleState updateEventState(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado: " + eventId));

        LifeCycleState newState = calculateNewState(event);
        event.setState(newState);
        eventRepository.save(event);

        log.info("🔄 Evento [{}] - {} estado manual atualizado para: {}",
                eventId, event.getName(), newState);

        return newState;
    }

    /**
     * Calcula o novo estado baseado nas regras de negócio
     */
    private LifeCycleState calculateNewState(Event event) {
        LocalDateTime now = LocalDateTime.now();

        // REGRA 1: Evento cancelado -> BANNED
        if (Boolean.TRUE.equals(event.getIsCancelled())) {
            return LifeCycleState.BANNED;
        }

        // REGRA 2: Evento já ocorreu -> INACTIVE
        if (event.getEventDate() != null && event.getEventDate().isBefore(now)) {
            return LifeCycleState.INACTIVE;
        }

        // Caso contrário -> ACTIVE
        return LifeCycleState.ACTIVE;
    }
}