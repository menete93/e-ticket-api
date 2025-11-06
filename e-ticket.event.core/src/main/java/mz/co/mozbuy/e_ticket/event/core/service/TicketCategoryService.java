package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketCategoryDTO;
import mz.co.mozbuy.e_ticket.event.core.model.TicketCategoryEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.TicketCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface TicketCategoryService {


    public TicketCategoryEntity create(TicketCategoryDTO entity);

    public TicketCategoryEntity update(TicketCategoryEntity entity) ;

    public Optional<TicketCategoryEntity> findById(Long id) ;

    public List<TicketCategoryEntity> findAll() ;

    public void delete(Long id) ;
}
