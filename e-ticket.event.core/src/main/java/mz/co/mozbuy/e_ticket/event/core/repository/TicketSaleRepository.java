package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketSaleRepository extends JpaRepository<TicketSale, Long> {

    Optional<TicketSale> findByTransactionId(String transactionId);

    List<TicketSale> findByEventId(Long eventId);

    List<TicketSale> findByOrganizerId(Long organizerId);

//    List<TicketSale> findByTicketId(Long ticketId);

    List<TicketSale> findByBuyerEmail(String buyerEmail);

    @Query("SELECT s FROM TicketSale s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<TicketSale> findByPeriod(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM TicketSale s WHERE s.organizer.id = :organizerId AND " +
            "s.createdAt BETWEEN :startDate AND :endDate")
    List<TicketSale> findByOrganizerAndPeriod(@Param("organizerId") Long organizerId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM TicketSale s WHERE s.event.id = :eventId AND " +
            "s.createdAt BETWEEN :startDate AND :endDate")
    List<TicketSale> findByEventAndPeriod(@Param("eventId") Long eventId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM TicketSale s WHERE s.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(s.totalAmount) FROM TicketSale s WHERE s.event.id = :eventId AND s.status = 'PAID'")
    BigDecimal sumTotalSalesByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(s.commissionAmount) FROM TicketSale s WHERE s.event.id = :eventId AND s.status = 'PAID'")
    BigDecimal sumCommissionByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(s.totalAmount) FROM TicketSale s WHERE s.organizer.id = :organizerId AND s.status = 'PAID'")
    BigDecimal sumTotalSalesByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT SUM(s.commissionAmount) FROM TicketSale s WHERE s.organizer.id = :organizerId AND s.status = 'PAID'")
    BigDecimal sumCommissionByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(s) FROM TicketSale s WHERE s.isTrialEvent = true")
    long countTrialEventSales();

    // Contar compras por email do comprador
    long countByBuyerEmail(String buyerEmail);

    // Buscar compras por email e status
    List<TicketSale> findByBuyerEmailAndStatus(String buyerEmail, String status);

    // Somar total gasto por email
    @Query("SELECT COALESCE(SUM(ts.totalAmount), 0) FROM TicketSale ts WHERE ts.buyerEmail = :email AND ts.status = 'PAID'")
    BigDecimal sumTotalAmountByBuyerEmailAndStatus(@Param("email") String email);
}