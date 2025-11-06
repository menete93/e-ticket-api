package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategoryEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.EventCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventCategoryService {

    private final EventCategoryRepository repository;

    public EventCategoryEntity create(EventCategoryEntity category) {

        EventCategoryEntity eventCategoryEntity = repository.save(category);

        return eventCategoryEntity;
    }

    public EventCategoryEntity update(EventCategoryEntity category) {
        return repository.save(category);
    }

    public Optional<EventCategoryEntity> findById(Long id) {
        return repository.findById(id);
    }

    public List<EventCategoryEntity> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
