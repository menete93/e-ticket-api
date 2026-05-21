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

    @Query("SELECT e FROM Event e WHERE e.createdBy = :username AND e.state = 'ACTIVE' ")
    List<Event> findActiveEventsByUser(@Param("username") String username);

        @Lock(LockModeType.PESSIMISTIC_WRITE) // Evita concorrência
        @Query("SELECT e FROM Event e LEFT JOIN FETCH e.tickets WHERE e.id = :id")
        Optional<Event> findByIdWithLock(@Param("id") Long id);

    // TESTE 1: Query Nativa DIRETA (deve funcionar)
    @Query(value = "SELECT * FROM e_ticket.events WHERE state = 'ACTIVE'",
            nativeQuery = true)
    List<Event> findActiveNative();

    // TESTE 2: Query Nativa com CAST (para garantir)
    @Query(value = "SELECT * FROM e_ticket.events WHERE state::TEXT = 'ACTIVE'",
            nativeQuery = true)
    List<Event> findActiveNativeCast();

    // TESTE 3: Método derivado SIMPLES
    List<Event> findByState(LifeCycleState lifeCycleState);

//    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.tickets WHERE e.lifeCycleState = 'ACTIVE'")
//    List<Event> findActiveEventsWithTickets();





    @Query("""
        SELECT new mz.co.mozbuy.e_ticket.event.core.dto.EventSimpleDTO(
            e.id,
            e.name,
            e.description,
            e.state,
            e.eventDate
        )
        FROM Event e 
        WHERE e.state = 'ACTIVE'
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
        WHERE e.state = 'ACTIVE'
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
            "WHERE e.state = :state")
    List<Event> findByStateWithTickets(@Param("state") LifeCycleState state);


    @Query("""
    SELECT e FROM Event e
    LEFT JOIN FETCH e.tickets
    WHERE e.state = 'ACTIVE'
      AND e.organizer.referenceId = :referenceId
""")
    List<Event> findActiveEventsByOrganizerWithTickets(@Param("referenceId") String referenceId);



    @Query("""
    SELECT e FROM Event e
    LEFT JOIN FETCH e.tickets
    WHERE e.organizer.referenceId = :referenceId
""")
    List<Event> findAll(@Param("referenceId") String referenceId);

    @Query("""
    SELECT e FROM Event e
    LEFT JOIN FETCH e.tickets
    WHERE e.state = 'ACTIVE'
      AND e.organizer.referenceId = :referenceId
""")
    List<Event> findAllForOrganizer(@Param("referenceId") String referenceId);

    // Buscar eventos com prazo expirado
    @Query("SELECT e FROM Event e WHERE e.registrationDeadline < :now " +
            "AND e.soldTickets < COALESCE(e.minAttendees, 1) " +
            "AND e.state = 'ACTIVE'")
    List<Event> findByRegistrationDeadlineBeforeAndSoldTicketsLessThanMinAttendees(@Param("now") LocalDateTime now);

    // Buscar eventos que podem ser reativados
    List<Event> findByIsCancelledFalseAndEventDateAfterAndState(LocalDateTime date, LifeCycleState state);

    // Buscar eventos prestes a expirar
    List<Event> findByRegistrationDeadlineBetweenAndState(LocalDateTime start, LocalDateTime end, LifeCycleState state);

    // Buscar eventos com baixa adesão
    @Query("SELECT e FROM Event e WHERE e.eventDate BETWEEN :startDate AND :endDate " +
            "AND e.state = 'ACTIVE' " +
            "AND e.maxAttendees IS NOT NULL " +
            "AND (e.soldTickets * 100.0 / e.maxAttendees) < :percentageThreshold")
    List<Event> findEventsWithLowAttendance(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("percentageThreshold") int percentageThreshold);



    /**
     * Buscar eventos cancelados que NÃO estão no estado BANNED
     */
    @Query("SELECT e FROM Event e WHERE e.isCancelled = true AND e.state != :excludeState")
    List<Event> findByIsCancelledTrueAndStateNot(@Param("excludeState") LifeCycleState excludeState);

    /**
     * Buscar eventos cancelados por estado específico
     */
    List<Event> findByIsCancelledTrueAndState(LifeCycleState state);

    // ==================== REGRA 2: Eventos com data expirada ====================

    /**
     * Buscar eventos NÃO cancelados com data expirada
     */
    @Query("SELECT e FROM Event e WHERE e.eventDate < :now AND e.isCancelled = false")
    List<Event> findByEventDateBeforeAndIsCancelledFalse(@Param("now") LocalDateTime now);

    /**
     * Buscar eventos com data entre intervalo e NÃO cancelados
     */
    @Query("SELECT e FROM Event e WHERE e.eventDate BETWEEN :startDate AND :endDate AND e.isCancelled = false")
    List<Event> findByEventDateBetweenAndIsCancelledFalse(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Buscar eventos com data expirada e estado específico
     */
    @Query("SELECT e FROM Event e WHERE e.eventDate < :now AND e.state = :state")
    List<Event> findByEventDateBeforeAndState(@Param("now") LocalDateTime now,
                                              @Param("state") LifeCycleState state);


    // EventRepository.java - Adicione estes métodos no final da interface

    // ==================== MÉTODOS PARA EVENTOS CANCELADOS E EXPIRADOS ====================

    /**
     * Buscar eventos cancelados (isCancelled = true)
     */
    List<Event> findByIsCancelledTrue();

    /**
     * Buscar eventos NÃO cancelados com data futura (para landing page)
     */
    @Query("SELECT e FROM Event e WHERE e.isCancelled = false AND e.eventDate > :now AND e.state = 'ACTIVE'")
    List<Event> findByIsCancelledFalseAndEventDateAfter(@Param("now") LocalDateTime now);

    /**
     * Buscar eventos NÃO cancelados com data futura (com tickets carregados)
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.tickets WHERE e.isCancelled = false AND e.eventDate > :now AND e.state = 'ACTIVE'")
    List<Event> findActiveEventsWithTicketsForLanding(@Param("now") LocalDateTime now);

    /**
     * Buscar eventos expirados (data passada, não cancelados, não BANNED)
     */
    @Query("SELECT e FROM Event e WHERE e.eventDate < :now AND e.isCancelled = false AND e.state != 'INACTIVE'")
    List<Event> findExpiredEventsNotUpdated(@Param("now") LocalDateTime now);

    /**
     * Contar eventos por estado
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.state = :state")
    long countByState(@Param("state") LifeCycleState state);

    /**
     * Buscar eventos por organizador e estado
     */
    @Query("SELECT e FROM Event e WHERE e.organizer.referenceId = :referenceId AND e.state = :state")
    List<Event> findByOrganizerReferenceIdAndState(@Param("referenceId") String referenceId,
                                                   @Param("state") LifeCycleState state);

    /**
     * Buscar eventos por organizador e data
     */
    @Query("SELECT e FROM Event e WHERE e.organizer.referenceId = :referenceId AND e.eventDate BETWEEN :startDate AND :endDate")
    List<Event> findByOrganizerAndDateRange(@Param("referenceId") String referenceId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * Buscar eventos com tickets vendidos
     */
    @Query("SELECT e FROM Event e WHERE e.soldTickets > 0 AND e.state = 'ACTIVE'")
    List<Event> findEventsWithSoldTickets();

    /**
     * Buscar eventos por nome e estado
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) AND e.state = :state")
    List<Event> findByNameContainingAndState(@Param("name") String name,
                                             @Param("state") LifeCycleState state);

    /**
     * Atualizar estado em lote (bulk update)
     */
    @Modifying
    @Query("UPDATE Event e SET e.state = :newState WHERE e.id IN :eventIds")
    int bulkUpdateState(@Param("eventIds") List<Long> eventIds,
                        @Param("newState") LifeCycleState newState);

    /**
     * Atualizar eventos expirados para INACTIVE (bulk update)
     */
    @Modifying
    @Query("UPDATE Event e SET e.state = 'INACTIVE' WHERE e.eventDate < :now AND e.isCancelled = false AND e.state = 'ACTIVE'")
    int bulkUpdateExpiredToInactive(@Param("now") LocalDateTime now);

    /**
     * Atualizar eventos cancelados para BANNED (bulk update)
     */
    @Modifying
    @Query("UPDATE Event e SET e.state = 'BANNED' WHERE e.isCancelled = true AND e.state != 'BANNED'")
    int bulkUpdateCancelledToBanned();




}