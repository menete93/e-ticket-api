package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.CustomerPurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPurchaseHistoryRepository extends JpaRepository<CustomerPurchaseHistory, Long> {

    Optional<CustomerPurchaseHistory> findByUserId(Long userId);

    List<CustomerPurchaseHistory> findByLoyaltyTier(String tier);

    @Query("SELECT c FROM CustomerPurchaseHistory c WHERE c.totalSpent >= :minSpent")
    List<CustomerPurchaseHistory> findByMinSpent(@Param("minSpent") BigDecimal minSpent);

    @Query("SELECT c FROM CustomerPurchaseHistory c WHERE c.totalPurchases >= :minPurchases")
    List<CustomerPurchaseHistory> findByMinPurchases(@Param("minPurchases") Integer minPurchases);

    // Top clientes
    List<CustomerPurchaseHistory> findTop10ByOrderByTotalSpentDesc();

    List<CustomerPurchaseHistory> findTop10ByOrderByTotalPurchasesDesc();

    // Estatísticas
    @Query("SELECT AVG(c.totalSpent) FROM CustomerPurchaseHistory c")
    Double getAverageCustomerSpend();

    @Query("SELECT COUNT(c) FROM CustomerPurchaseHistory c WHERE c.loyaltyTier = :tier")
    Long countByLoyaltyTier(@Param("tier") String tier);


}