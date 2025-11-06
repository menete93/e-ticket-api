package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.EventRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.model.EventEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.EventCategoryRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.EventRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{

    private final EventRepository eventRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final EventCategoryRepository categoryRepository;



    @Override
    public EventEntity create(EventRequestDTO dto) {
        EventEntity event = new EventEntity();

        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setCapacity(dto.getCapacity());
        event.setSeatType(dto.getSeatType());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setCreatedBy(dto.getCreatedBy());

        // Criar Point a partir de latitude e longitude
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            Point point = geometryFactory.createPoint(
                    new Coordinate(dto.getLongitude(), dto.getLatitude())
            );
            point.setSRID(4326);
            event.setLocation(point);
        }

        // Associar categoria
        if (dto.getCategory_id() != null) {
            categoryRepository.findById(dto.getCategory_id())
                    .ifPresent(event::setCategory);
        }

        return eventRepository.save(event);

    }

    @Override
    public EventEntity update(EventEntity event) {
        return null;
    }

    @Override
    public Optional<EventEntity> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<EventEntity> findAll() {
        return List.of();
    }

    @Override
    public void delete(Long id) {

    }
}
