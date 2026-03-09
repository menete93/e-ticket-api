package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.CustomerLoyaltyDetailDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.LoyaltyReportDTO;
import mz.co.mozbuy.e_ticket.event.core.model.CustomerPurchaseHistory;
import mz.co.mozbuy.e_ticket.event.core.model.Event;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import mz.co.mozbuy.e_ticket.event.core.repository.CustomerPurchaseHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final CustomerPurchaseHistoryRepository purchaseHistoryRepository;

    // ==================== MÉTODOS DE CONSULTA ====================

    /**
     * Busca histórico de um cliente
     */
    public Optional<CustomerPurchaseHistory> getCustomerHistory(Long userId) {
        return purchaseHistoryRepository.findByUserId(userId);
    }

    /**
     * Busca os maiores gastadores
     */
    public List<CustomerPurchaseHistory> getTopSpenders() {
        return purchaseHistoryRepository.findTop10ByOrderByTotalSpentDesc();
    }

    /**
     * Conta clientes por tier
     */
    public Long countByTier(String tier) {
        return purchaseHistoryRepository.countByLoyaltyTier(tier);
    }

    // ==================== MÉTODOS DE ATUALIZAÇÃO ====================

    /**
     * Atualiza histórico do cliente após uma compra
     */
    @Transactional
    public void updateCustomerHistory(Long userId, EventTicket ticket, Integer quantity,
                                      BigDecimal totalAmount, Event event) {
        CustomerPurchaseHistory history = purchaseHistoryRepository
                .findByUserId(userId)
                .orElseGet(() -> createNewHistory(userId));

        history.recordPurchase(quantity, totalAmount, LocalDateTime.now(), event);

        // Atualiza categoria favorita baseada no ticket atual
        updateFavoriteCategory(history, ticket.getCategory().name());

        purchaseHistoryRepository.save(history);

        log.info("✅ Updated purchase history for user {}: total purchases now {}, tier: {}",
                userId, history.getTotalPurchases(), history.getLoyaltyTier());
    }

    /**
     * Cria novo histórico para cliente
     */
    private CustomerPurchaseHistory createNewHistory(Long userId) {
        return CustomerPurchaseHistory.builder()
                .userId(userId)
                .totalPurchases(0)
                .totalTicketsBought(0)
                .totalSpent(BigDecimal.ZERO)
                .loyaltyTier("NEW")
                .firstPurchaseDate(null)
                .lastPurchaseDate(null)
                .build();
    }

    /**
     * Atualiza categoria favorita do cliente
     */
    private void updateFavoriteCategory(CustomerPurchaseHistory history, String category) {
        // Lógica simples: se não tem categoria favorita, define esta
        if (history.getFavoriteCategory() == null) {
            history.setFavoriteCategory(category);
        }
        // Poderia ter lógica mais complexa de contagem de categorias
    }

    // ==================== MÉTODOS DE VALIDAÇÃO DE ELEGIBILIDADE ====================

    /**
     * Verifica se cliente é elegível para uma estratégia de fidelidade
     */
    public boolean isEligibleForLoyaltyStrategy(Long userId, PricingStrategy strategy) {
        if (userId == null) return false;

        CustomerPurchaseHistory history = purchaseHistoryRepository
                .findByUserId(userId)
                .orElse(null);

        // Se não tem histórico e estratégia é apenas para repetição
        if (history == null && strategy.isRepeatBuyerOnly()) {
            return false;
        }

        // Se tem histórico e estratégia é apenas para primeira compra
        if (history != null && strategy.isFirstTimeBuyerOnly()) {
            return false;
        }

        if (history == null) {
            return !strategy.isRepeatBuyerOnly(); // Novo cliente, só se não for repeat only
        }

        // Verificar tier
        if (strategy.getLoyaltyTier() != null &&
                !strategy.getLoyaltyTier().equals(history.getLoyaltyTier())) {
            return false;
        }

        // Verificar número mínimo de compras
        if (strategy.getMinPurchases() != null &&
                history.getTotalPurchases() < strategy.getMinPurchases()) {
            return false;
        }

        // Verificar valor mínimo gasto
        if (strategy.getMinTotalSpent() != null &&
                history.getTotalSpent().compareTo(strategy.getMinTotalSpent()) < 0) {
            return false;
        }

        return true;
    }

    // ==================== MÉTODOS DE CÁLCULO DE DESCONTO ====================

    /**
     * Calcula desconto baseado no tier do cliente
     */
    public BigDecimal getLoyaltyDiscount(Long userId, BigDecimal originalPrice) {
        CustomerPurchaseHistory history = purchaseHistoryRepository
                .findByUserId(userId)
                .orElse(null);

        if (history == null) return BigDecimal.ZERO;

        BigDecimal discount = switch (history.getLoyaltyTier()) {
            case "PLATINUM" -> originalPrice.multiply(new BigDecimal("0.20")); // 20% off
            case "GOLD" -> originalPrice.multiply(new BigDecimal("0.15"));     // 15% off
            case "SILVER" -> originalPrice.multiply(new BigDecimal("0.10"));   // 10% off
            case "BRONZE" -> originalPrice.multiply(new BigDecimal("0.05"));   // 5% off
            default -> BigDecimal.ZERO;
        };

        log.debug("Loyalty discount for user {} (tier: {}): {}",
                userId, history.getLoyaltyTier(), discount);

        return discount;
    }

    /**
     * Calcula desconto baseado em número de compras
     */
    public BigDecimal getVolumeDiscount(Long userId, BigDecimal originalPrice) {
        CustomerPurchaseHistory history = purchaseHistoryRepository
                .findByUserId(userId)
                .orElse(null);

        if (history == null) return BigDecimal.ZERO;

        int purchases = history.getTotalPurchases();
        BigDecimal discount = BigDecimal.ZERO;

        if (purchases >= 20) {
            discount = originalPrice.multiply(new BigDecimal("0.25")); // 25% off
        } else if (purchases >= 10) {
            discount = originalPrice.multiply(new BigDecimal("0.20")); // 20% off
        } else if (purchases >= 5) {
            discount = originalPrice.multiply(new BigDecimal("0.15")); // 15% off
        } else if (purchases >= 3) {
            discount = originalPrice.multiply(new BigDecimal("0.10")); // 10% off
        }

        return discount;
    }

    // ==================== MÉTODOS DE RELATÓRIO ====================

    /**
     * Gera relatório completo de fidelidade
     */
    public LoyaltyReportDTO generateLoyaltyReport() {
        long platinum = purchaseHistoryRepository.countByLoyaltyTier("PLATINUM");
        long gold = purchaseHistoryRepository.countByLoyaltyTier("GOLD");
        long silver = purchaseHistoryRepository.countByLoyaltyTier("SILVER");
        long bronze = purchaseHistoryRepository.countByLoyaltyTier("BRONZE");
        long newCustomers = purchaseHistoryRepository.countByLoyaltyTier("NEW");
        long total = platinum + gold + silver + bronze + newCustomers;

        Double averageSpend = purchaseHistoryRepository.getAverageCustomerSpend();
        List<CustomerPurchaseHistory> topSpenders = purchaseHistoryRepository.findTop10ByOrderByTotalSpentDesc();

        return LoyaltyReportDTO.builder()
                .platinumCustomers(platinum)
                .goldCustomers(gold)
                .silverCustomers(silver)
                .bronzeCustomers(bronze)
                .newCustomers(newCustomers)
                .totalCustomers(total)
                .averageCustomerSpend(averageSpend != null ? averageSpend : 0.0)
                .topCustomers(topSpenders)
                .platinumPercentage(total > 0 ? (platinum * 100.0 / total) : 0)
                .goldPercentage(total > 0 ? (gold * 100.0 / total) : 0)
                .silverPercentage(total > 0 ? (silver * 100.0 / total) : 0)
                .bronzePercentage(total > 0 ? (bronze * 100.0 / total) : 0)
                .build();
    }

    /**
     * Gera relatório de um cliente específico
     */
    public CustomerLoyaltyDetailDTO getCustomerLoyaltyDetail(Long userId) {
        CustomerPurchaseHistory history = purchaseHistoryRepository
                .findByUserId(userId)
                .orElse(null);

        if (history == null) {
            return CustomerLoyaltyDetailDTO.builder()
                    .userId(userId)
                    .tier("NEW")
                    .message("Cliente novo sem histórico de compras")
                    .build();
        }

        BigDecimal nextTierDiscount = getNextTierDiscount(history);
        BigDecimal purchasesToNextTier = getPurchasesToNextTier(history);
        BigDecimal spentToNextTier = getSpentToNextTier(history);

        return CustomerLoyaltyDetailDTO.builder()
                .userId(userId)
                .tier(history.getLoyaltyTier())
                .totalPurchases(history.getTotalPurchases())
                .totalSpent(history.getTotalSpent())
                .averageTicketValue(history.getAverageTicketValue())
                .favoriteCategory(history.getFavoriteCategory())
                .firstPurchaseDate(history.getFirstPurchaseDate())
                .lastPurchaseDate(history.getLastPurchaseDate())
                .currentDiscount(getLoyaltyDiscount(userId, BigDecimal.valueOf(100)))
                .nextTierDiscount(nextTierDiscount)
                .purchasesToNextTier(purchasesToNextTier)
                .spentToNextTier(spentToNextTier)
                .build();
    }

    /**
     * Calcula desconto do próximo tier
     */
    private BigDecimal getNextTierDiscount(CustomerPurchaseHistory history) {
        return switch (history.getLoyaltyTier()) {
            case "BRONZE" -> new BigDecimal("10"); // 10% (SILVER)
            case "SILVER" -> new BigDecimal("15"); // 15% (GOLD)
            case "GOLD" -> new BigDecimal("20");   // 20% (PLATINUM)
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Calcula compras necessárias para próximo tier
     */
    private BigDecimal getPurchasesToNextTier(CustomerPurchaseHistory history) {
        int purchases = history.getTotalPurchases();
        return switch (history.getLoyaltyTier()) {
            case "BRONZE" -> BigDecimal.valueOf(5 - purchases).max(BigDecimal.ZERO);
            case "SILVER" -> BigDecimal.valueOf(10 - purchases).max(BigDecimal.ZERO);
            case "GOLD" -> BigDecimal.valueOf(20 - purchases).max(BigDecimal.ZERO);
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Calcula valor necessário para próximo tier
     */
    private BigDecimal getSpentToNextTier(CustomerPurchaseHistory history) {
        BigDecimal spent = history.getTotalSpent();
        return switch (history.getLoyaltyTier()) {
            case "BRONZE" -> new BigDecimal("500").subtract(spent).max(BigDecimal.ZERO);
            case "SILVER" -> new BigDecimal("2000").subtract(spent).max(BigDecimal.ZERO);
            case "GOLD" -> new BigDecimal("5000").subtract(spent).max(BigDecimal.ZERO);
            default -> BigDecimal.ZERO;
        };
    }
}