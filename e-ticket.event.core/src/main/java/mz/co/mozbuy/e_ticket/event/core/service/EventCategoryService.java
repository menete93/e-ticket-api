package mz.co.mozbuy.e_ticket.event.core.service;



import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.EventCategoryDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.EventCategoryRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategory;
import mz.co.mozbuy.e_ticket.event.core.repository.EventCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;

    /**
     * Encontra todas as categorias ativas
     */
    public List<EventCategoryDTO> findAllActive() {
        return eventCategoryRepository.findByLifeCycleState(LifeCycleState.ACTIVE)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Encontra categoria por ID
     */
    public EventCategoryDTO findById(Long id) {
        EventCategory category = eventCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toDTO(category);
    }

    /**
     * Encontra categoria por nome
     */
    public EventCategoryDTO findByName(String name) {
        EventCategory category = eventCategoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
        return toDTO(category);
    }

    /**
     * Cria uma nova categoria
     */
    @Transactional
    public EventCategoryDTO create(EventCategoryRequestDTO requestDTO) {
        // Verificar se já existe categoria com mesmo nome
        if (eventCategoryRepository.existsByNameIgnoreCase(requestDTO.getName())) {
            throw new RuntimeException("Category with name '" + requestDTO.getName() + "' already exists");
        }

        EventCategory category = new EventCategory();
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setColorCode(requestDTO.getColorCode());
        category.setIconUrl(requestDTO.getIconUrl());
        category.setCreatedAt(LocalDateTime.now());

        EventCategory savedCategory = eventCategoryRepository.save(category);
        log.info("Created event category: {}", savedCategory.getName());

        return toDTO(savedCategory);
    }

    /**
     * Atualiza uma categoria existente
     */
    @Transactional
    public EventCategoryDTO update(Long id, EventCategoryRequestDTO requestDTO) {
        EventCategory category = eventCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Verificar se outro categoria tem o mesmo nome
        if (!category.getName().equalsIgnoreCase(requestDTO.getName()) &&
                eventCategoryRepository.existsByNameIgnoreCaseAndIdNot(requestDTO.getName(), id)) {
            throw new RuntimeException("Category with name '" + requestDTO.getName() + "' already exists");
        }

        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setColorCode(requestDTO.getColorCode());
        category.setIconUrl(requestDTO.getIconUrl());

        EventCategory updatedCategory = eventCategoryRepository.save(category);
        log.info("Updated event category: {}", updatedCategory.getName());

        return toDTO(updatedCategory);
    }

    /**
     * Desativa uma categoria
     */
    @Transactional
    public void deactivate(Long id) {
        EventCategory category = eventCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Verificar se a categoria tem eventos ativos
        Long eventCount = eventCategoryRepository.countEventsByCategoryId(id);
        if (eventCount > 0) {
            throw new RuntimeException("Cannot deactivate category with active events. Move or delete events first.");
        }

//        category.setIsActive(false);
        eventCategoryRepository.save(category);
        log.info("Deactivated event category: {}", category.getName());
    }

    /**
     * Busca categorias por termo
     */
    public List<EventCategoryDTO> search(String term) {
        return eventCategoryRepository.findByNameContainingIgnoreCase(term)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca categorias populares
     */
    public List<EventCategoryDTO> findPopularCategories() {
        return eventCategoryRepository.findPopularCategories()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte entidade para DTO
     */
    private EventCategoryDTO toDTO(EventCategory category) {
        EventCategoryDTO dto = new EventCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setColorCode(category.getColorCode());
        dto.setIconUrl(category.getIconUrl());
        dto.setEventsCount(category.getEvents() != null ? category.getEvents().size() : 0);
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setCreatedBy(category.getCreatedBy());
        dto.setUpdatedBy(category.getUpdatedBy());
        return dto;
    }
}