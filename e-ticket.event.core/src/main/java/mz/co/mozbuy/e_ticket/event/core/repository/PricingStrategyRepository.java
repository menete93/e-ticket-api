package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingStrategyRepository extends JpaRepository<PricingStrategy, Long> {

    List<PricingStrategy> findByEventId(Long eventId);

    @Query("SELECT p FROM PricingStrategy p WHERE p.event.id = :eventId AND p.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    List<PricingStrategy> findActiveByEventId(@Param("eventId") Long eventId);

    @Query("SELECT p FROM PricingStrategy p WHERE p.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    List<PricingStrategy> findAllActive();

    @Query("SELECT p FROM PricingStrategy p WHERE p.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND p.applyAutomatically = true")
    List<PricingStrategy> findActiveAndAutoApplied();

    @Query("SELECT ps FROM PricingStrategy ps WHERE ps.event.id = :eventId AND ps.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND ps.applyAutomatically = true")
    List<PricingStrategy> findActiveAutoApplyStrategiesByEventId(@Param("eventId") Long eventId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM PricingStrategy s " +
            "WHERE s.event.id = :eventId " +
            "AND s.strategyName = :strategyName " +
            "AND s.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    boolean existsActiveStrategy(@Param("eventId") Long eventId,
                                 @Param("strategyName") String strategyName);

    @Query("SELECT p FROM PricingStrategy p " +
            "WHERE p.event.id = :eventId " +
            "AND p.strategyType = :strategyType " +
            "AND p.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    Optional<PricingStrategy> findActiveByEventIdAndStrategyType(@Param("eventId") Long eventId,
                                                                 @Param("strategyType") String strategyType);
}