package mz.co.mozbuy.e_ticket.event.core.service.listeners;


import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import org.springframework.stereotype.Component;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Slf4j
@Component
public class EventCancelListener {

    @PrePersist
    public void beforeCreate(Event event) {
        // Garantir que isCancelled seja false ao criar
        if (event.getIsCancelled() == null) {
            event.setIsCancelled(false);
        }

        // Garantir que state seja ACTIVE ao criar
        if (event.getState() == null) {
            event.setState(LifeCycleState.ACTIVE);
        }

        log.debug("📝 Novo evento criado: {} - isCancelled={}, state={}",
                event.getName(), event.getIsCancelled(), event.getState());
    }

    @PreUpdate
    public void beforeUpdate(Event event) {
        // Se isCancelled mudou para true, atualiza state para BANNED
        if (Boolean.TRUE.equals(event.getIsCancelled())) {
            if (event.getState() != LifeCycleState.BANNED) {
                event.setState(LifeCycleState.BANNED);
                log.info("🚫 Evento [{}] - {} cancelado. State atualizado para BANNED",
                        event.getId(), event.getName());
            }
        }
    }
}
