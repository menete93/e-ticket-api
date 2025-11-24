package mz.co.mozbuy.e_ticket.event.core.mapper;



import mz.co.mozbuy.e_ticket.event.core.dto.EventDto;
import mz.co.mozbuy.e_ticket.event.core.dto.EventLocationDto;
import mz.co.mozbuy.e_ticket.event.core.model.EventEntity;
import org.locationtech.jts.geom.Point;

public class EventMapper {

    public static EventDto toDto(EventEntity event) {
        EventLocationDto locationDto = null;
        Point point = event.getLocation();
        if (point != null) {
            locationDto = new EventLocationDto(point.getY(), point.getX()); // Y = latitude, X = longitude
        }
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStatus(),
                locationDto
        );
    }
}
