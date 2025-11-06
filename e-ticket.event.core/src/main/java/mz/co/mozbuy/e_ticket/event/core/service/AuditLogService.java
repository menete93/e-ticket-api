package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.AuditLogEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogEntity create(AuditLogEntity entity) {
        return repository.save(entity);
    }

    public List<AuditLogEntity> findAll() {
        return repository.findAll();
    }

    public Optional<AuditLogEntity> findById(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
