package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.SalePhaseEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.SalePhaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalePhaseService {

    private final SalePhaseRepository repository;

    public SalePhaseEntity create(SalePhaseEntity entity) {
        return repository.save(entity);
    }

    public SalePhaseEntity update(SalePhaseEntity entity) {
        return repository.save(entity);
    }

    public Optional<SalePhaseEntity> findById(Long id) {
        return repository.findById(id);
    }

    public List<SalePhaseEntity> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
