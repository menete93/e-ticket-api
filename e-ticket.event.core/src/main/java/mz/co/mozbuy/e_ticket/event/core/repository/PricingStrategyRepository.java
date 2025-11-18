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

    List<PricingStrategy> findByEventIdAndIsActiveTrue(Long eventId);

    List<PricingStrategy> findByIsActiveTrue();

    List<PricingStrategy> findByIsActiveTrueAndApplyAutomaticallyTrue();

    @Query("SELECT ps FROM PricingStrategy ps WHERE ps.event.id = :eventId AND ps.isActive = true AND ps.applyAutomatically = true")
    List<PricingStrategy> findActiveAutoApplyStrategiesByEventId(@Param("eventId") Long eventId);

    boolean existsByEventIdAndStrategyNameAndIsActiveTrue(Long eventId, String strategyName);

    Optional<PricingStrategy> findByEventIdAndStrategyTypeAndIsActiveTrue(Long eventId, String strategyType);
}