package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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


    // ============================================
    // 🔥 QUERIES PARA ATUALIZAÇÃO PARCIAL
    // ============================================

    /**
     * Atualiza estatísticas do organizador após uma venda
     */
    @Modifying
    @Query("UPDATE Organizer o SET " +
            "o.totalEarnings = COALESCE(o.totalEarnings, 0) + :amount, " +
            "o.totalCommissionPaid = COALESCE(o.totalCommissionPaid, 0) + :commission, " +
            "o.totalTicketsSold = COALESCE(o.totalTicketsSold, 0) + :quantity, " +
            "o.accountBalance = COALESCE(o.accountBalance, 0) + :payout " +
            "WHERE o.id = :organizerId")
    void updateStats(
            @Param("organizerId") Long organizerId,
            @Param("amount") BigDecimal amount,
            @Param("commission") BigDecimal commission,
            @Param("quantity") Integer quantity,
            @Param("payout") BigDecimal payout
    );

    /**
     * Atualiza apenas o saldo da conta
     */
    @Modifying
    @Query("UPDATE Organizer o SET " +
            "o.accountBalance = COALESCE(o.accountBalance, 0) + :amount " +
            "WHERE o.id = :organizerId")
    void updateBalance(
            @Param("organizerId") Long organizerId,
            @Param("amount") BigDecimal amount
    );

    /**
     * Atualiza apenas ganhos totais
     */
    @Modifying
    @Query("UPDATE Organizer o SET " +
            "o.totalEarnings = COALESCE(o.totalEarnings, 0) + :amount " +
            "WHERE o.id = :organizerId")
    void addEarnings(
            @Param("organizerId") Long organizerId,
            @Param("amount") BigDecimal amount
    );

    /**
     * Atualiza apenas comissão paga
     */
    @Modifying
    @Query("UPDATE Organizer o SET " +
            "o.totalCommissionPaid = COALESCE(o.totalCommissionPaid, 0) + :commission " +
            "WHERE o.id = :organizerId")
    void addCommissionPaid(
            @Param("organizerId") Long organizerId,
            @Param("commission") BigDecimal commission
    );

    /**
     * Atualiza contagem de tickets vendidos
     */
    @Modifying
    @Query("UPDATE Organizer o SET " +
            "o.totalTicketsSold = COALESCE(o.totalTicketsSold, 0) + :quantity " +
            "WHERE o.id = :organizerId")
    void incrementTicketsSold(
            @Param("organizerId") Long organizerId,
            @Param("quantity") Integer quantity
    );

    /**
     * Atualiza o uso do trial
     */
    @Modifying
    @Query("UPDATE Organizer o SET " +
            "o.trialEventsRemaining = COALESCE(o.trialEventsRemaining, 0) - 1, " +
            "o.trialUsedCount = COALESCE(o.trialUsedCount, 0) + 1 " +
            "WHERE o.id = :organizerId AND o.trialEventsRemaining > 0")
    int consumeTrialEvent(@Param("organizerId") Long organizerId);

    /**
     * Atualiza o estado do organizador
     */
    @Modifying
    @Query("UPDATE Organizer o SET " +
            "o.state = :state " +
            "WHERE o.id = :organizerId")
    void updateState(
            @Param("organizerId") Long organizerId,
            @Param("state") LifeCycleState state
    );

    /**
     * Busca organizador com eventos
     */
    @Query("SELECT o FROM Organizer o " +
            "LEFT JOIN FETCH o.events " +
            "WHERE o.id = :id")
    Optional<Organizer> findByIdWithEvents(@Param("id") Long id);

    /**
     * Busca organizador com vendas
     */
    @Query("SELECT o FROM Organizer o " +
            "LEFT JOIN FETCH o.sales " +
            "WHERE o.id = :id")
    Optional<Organizer> findByIdWithSales(@Param("id") Long id);

}