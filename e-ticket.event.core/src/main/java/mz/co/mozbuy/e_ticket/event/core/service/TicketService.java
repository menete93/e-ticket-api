package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.TicketEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository repository;

    public TicketEntity create(TicketEntity entity) {
        return repository.save(entity);
    }

    public TicketEntity update(TicketEntity entity) {
        return repository.save(entity);
    }

    public Optional<TicketEntity> findById(Long id) {
        return repository.findById(id);
    }

    public List<TicketEntity> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
