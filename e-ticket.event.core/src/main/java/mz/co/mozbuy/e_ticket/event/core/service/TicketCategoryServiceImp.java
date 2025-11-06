package mz.co.mozbuy.e_ticket.event.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketCategoryDTO;
import mz.co.mozbuy.e_ticket.event.core.model.TicketCategoryEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.TicketCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketCategoryServiceImp implements TicketCategoryService {
    
    private final TicketCategoryRepository repository;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public TicketCategoryEntity create(TicketCategoryDTO dto) {
        TicketCategoryEntity category = new TicketCategoryEntity();

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setBasePrice(dto.getBasePrice());
        category.setCreatedBy(dto.getCreatedBy());
        category.setTotalQuantity(dto.getTotalQuantity());
        category.setAllocatedQuantity(dto.getAllocatedQuantity() != null ? dto.getAllocatedQuantity() : 0);

        // Conversão explícita para Map<String,Object> para garantir compatibilidade com jsonb
        if(dto.getInviteCondition() != null) {
            category.setInviteCondition(
                    objectMapper.convertValue(dto.getInviteCondition(), Map.class)
            );
        }

        if (dto.getEventId() != null) {
            eventRepository.findById(dto.getEventId())
                    .ifPresent(category::setEvent);
        }

        return repository.save(category);
    }

    @Override
    public TicketCategoryEntity update(TicketCategoryEntity entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<TicketCategoryEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<TicketCategoryEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }


}
