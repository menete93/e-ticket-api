package mz.co.mozbuy.e_ticket.event.core.service;
import mz.co.mozbuy.e_ticket.event.core.dto.EventRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.model.EventEntity;
import java.util.List;
import java.util.Optional;


public interface EventService {


    public EventEntity create(EventRequestDTO dto) ;

    public EventEntity update(EventEntity event);

    public Optional<EventEntity> findById(Long id) ;

    public List<EventEntity> findAll() ;

    public void delete(Long id);
}
