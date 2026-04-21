package mz.co.mozbuy.e_ticket.event.core.repository;


import jakarta.persistence.LockModeType;
import mz.co.mozbuy.e_ticket.event.core.dto.EventSimpleDTO;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findByCategory(EventCategory category);
    List<Event> findByCategoryId(Long categoryId);
    List<Event> findByNameContainingIgnoreCase(String name);
    List<Event> findByIsFeaturedTrue();
    List<Event> findByIsPublicTrue();
    List<Event> findByEventDateAfter(LocalDateTime date);

    @Query("SELECT e FROM Event e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Event> findByNameOrDescriptionContaining(@Param("term") String term);

    @Query(
            value = "SELECT * FROM event e WHERE ST_DWithin(e.geographic_location, ST_SetSRID(:point, 4326), :distance)",
            nativeQuery = true
    )
    List<Event> findByLocationNear(@Param("point") Point point, @Param("distance") double distance);


    @Query(value = "SELECT * FROM events WHERE ST_DWithin(geographic_location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius)", nativeQuery = true)
    List<Event> findEventsByRadius(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("radius") double radius);

    @Query("SELECT e FROM Event e WHERE e.eventDate BETWEEN :startDate AND :endDate")
    List<Event> findEventsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Event> findByCreatedBy(String createdBy);

    @Query("SELECT e FROM Event e WHERE e.createdBy = :username AND e.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    List<Event> findActiveEventsByUser(@Param("username") String username);

        @Lock(LockModeType.PESSIMISTIC_WRITE) // Evita concorrência
        @Query("SELECT e FROM Event e LEFT JOIN FETCH e.tickets WHERE e.id = :id")
        Optional<Event> findByIdWithLock(@Param("id") Long id);

    // TESTE 1: Query Nativa DIRETA (deve funcionar)
    @Query(value = "SELECT * FROM e_ticket.events WHERE life_cycle_state = 'ACTIVE'",
            nativeQuery = true)
    List<Event> findActiveNative();

    // TESTE 2: Query Nativa com CAST (para garantir)
    @Query(value = "SELECT * FROM e_ticket.events WHERE life_cycle_state::TEXT = 'ACTIVE'",
            nativeQuery = true)
    List<Event> findActiveNativeCast();

    // TESTE 3: Método derivado SIMPLES
    List<Event> findByLifeCycleState(LifeCycleState lifeCycleState);

//    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.tickets WHERE e.lifeCycleState = 'ACTIVE'")
//    List<Event> findActiveEventsWithTickets();





    @Query("""
        SELECT new mz.co.mozbuy.e_ticket.event.core.dto.EventSimpleDTO(
            e.id,
            e.name,
            e.description,
            e.lifeCycleState,
            e.eventDate
        )
        FROM Event e 
        WHERE e.lifeCycleState = 'ACTIVE'
        """)
    List<EventSimpleDTO> findActiveEventsSimple();

    @Query("""
        SELECT 
            e.id as eventId,
            e.name as eventName,
            t.id as ticketId,
            t.ticketName as ticketName,
            t.currentPrice as ticketPrice
        FROM Event e 
        LEFT JOIN e.tickets t
        WHERE e.lifeCycleState = 'ACTIVE'
        """)
    List<EventWithTicketsProjection> findActiveEventsWithTickets();

    interface EventWithTicketsProjection {
        Long getEventId();
        String getEventName();
        Long getTicketId();
        String getTicketName();
        BigDecimal getTicketPrice();
    }

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.tickets " +
            "LEFT JOIN FETCH e.organizer " +
            "LEFT JOIN FETCH e.category " +
            "WHERE e.id = :id")
    Optional<Event> findByIdWithTickets(@Param("id") Long id);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.tickets " +
            "WHERE e.lifeCycleState = :state")
    List<Event> findByStateWithTickets(@Param("state") LifeCycleState state);


    @Query("""
    SELECT e FROM Event e
    LEFT JOIN FETCH e.tickets
    WHERE e.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE
      AND e.organizer.referenceId = :referenceId
""")
    List<Event> findActiveEventsByOrganizerWithTickets(@Param("referenceId") String referenceId);


}