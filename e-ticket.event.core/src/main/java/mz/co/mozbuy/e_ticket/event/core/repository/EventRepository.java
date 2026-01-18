package mz.co.mozbuy.e_ticket.event.core.repository;


import jakarta.persistence.LockModeType;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.common.audit.LifeCycleState;
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


//    @Query("""
//    SELECT DISTINCT e
//    FROM Event e
//    LEFT JOIN FETCH e.tickets
//    WHERE e.lifeCycleState = :state
//""")
@EntityGraph(attributePaths = {"tickets"})
List<Event> findByLifeCycleState(@Param("state") LifeCycleState state);




}