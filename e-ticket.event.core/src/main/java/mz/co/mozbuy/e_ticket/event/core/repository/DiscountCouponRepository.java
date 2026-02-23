package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.DiscountCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

    Optional<DiscountCoupon> findByCodeAndEventId(String code, Long eventId);
    List<DiscountCoupon> findByEventId(Long eventId);


    List<DiscountCoupon> findByEventIdAndIsActiveTrue(Long eventId);

    List<DiscountCoupon> findByEventIdAndValidUntilAfter(Long eventId, LocalDateTime date);

    @Query("SELECT c FROM DiscountCoupon c WHERE c.event.id = :eventId AND c.isActive = true AND " +
            "c.validFrom <= :now AND (c.validUntil IS NULL OR c.validUntil >= :now) AND " +
            "(c.maxUses IS NULL OR c.usedCount < c.maxUses)")
    List<DiscountCoupon> findValidCouponsForEvent(@Param("eventId") Long eventId,
                                                  @Param("now") LocalDateTime now);

    @Query("SELECT c FROM DiscountCoupon c WHERE c.event.id = :eventId AND c.isActive = true AND " +
            "c.validFrom <= :now AND (c.validUntil IS NULL OR c.validUntil >= :now)")
    List<DiscountCoupon> findActiveCouponsForEvent(@Param("eventId") Long eventId,
                                                   @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(c) FROM DiscountCoupon c WHERE c.event.id = :eventId AND c.isActive = true")
    long countActiveCouponsForEvent(@Param("eventId") Long eventId);

    @Query("SELECT SUM(c.usedCount) FROM DiscountCoupon c WHERE c.event.id = :eventId")
    Integer sumUsedCouponsForEvent(@Param("eventId") Long eventId);

    boolean existsByCodeAndEventId(String code, Long eventId);
}