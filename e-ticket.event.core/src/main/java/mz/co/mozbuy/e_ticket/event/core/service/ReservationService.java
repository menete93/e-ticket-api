package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.ReservationEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repository;

    public ReservationEntity create(ReservationEntity entity) {
        return repository.save(entity);
    }

    public ReservationEntity update(ReservationEntity entity) {
        return repository.save(entity);
    }

    public Optional<ReservationEntity> findById(Long id) {
        return repository.findById(id);
    }

    public List<ReservationEntity> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
