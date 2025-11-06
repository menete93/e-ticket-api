package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;

    public PaymentEntity create(PaymentEntity entity) {
        return repository.save(entity);
    }

    public PaymentEntity update(PaymentEntity entity) {
        return repository.save(entity);
    }

    public Optional<PaymentEntity> findById(Long id) {
        return repository.findById(id);
    }

    public List<PaymentEntity> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
