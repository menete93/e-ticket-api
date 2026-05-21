package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

    Optional<Organizer> findByEmail(String email);

    Optional<Organizer> findByName(String name);

    List<Organizer> findByState(LifeCycleState lifeCycleState);

    @Query("SELECT o FROM Organizer o WHERE o.totalEarnings >= :minEarnings")
    List<Organizer> findTopEarners(@Param("minEarnings") BigDecimal minEarnings);

    @Query("SELECT COUNT(o) FROM Organizer o WHERE o.trialEventsRemaining > 0")
    long countOrganizersInTrial();

    @Query("SELECT SUM(o.totalEarnings) FROM Organizer o")
    BigDecimal sumTotalEarnings();

    @Query("SELECT SUM(o.totalCommissionPaid) FROM Organizer o")
    BigDecimal sumTotalCommissionPaid();

    boolean existsByEmail(String email);

    boolean existsByNuit(String nuit);

    Optional<Organizer> findByUserId(Long userId);



}