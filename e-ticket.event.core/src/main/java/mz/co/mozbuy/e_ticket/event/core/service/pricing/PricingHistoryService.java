package mz.co.mozbuy.e_ticket.event.core.service.pricing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketPriceChangeDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.TicketPriceHistoryDTO;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.model.TicketPriceHistory;
import mz.co.mozbuy.e_ticket.event.core.repository.EventTicketRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.PricingStrategyRepository;
import mz.co.mozbuy.e_ticket.event.core.repository.TicketPriceHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingHistoryService {

    private final TicketPriceHistoryRepository historyRepository;
    private final PricingStrategyRepository pricingStrategyRepository;
    private final EventTicketRepository eventTicketRepository;

    // ==================== MÉTODOS DE CONSULTA ====================

    @Transactional(readOnly = true)
    public List<TicketPriceHistoryDTO> getHistoryByTicket(Long ticketId) {
        return historyRepository.findByEventTicketIdOrderByChangedAtDesc(ticketId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketPriceHistoryDTO> getHistoryByEvent(Long eventId) {
        return historyRepository.findByEventTicketEventId(eventId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketPriceHistoryDTO> getHistoryByTicketAndDateRange(
            Long ticketId, LocalDateTime start, LocalDateTime end) {
        return historyRepository.findByTicketIdAndDateRange(ticketId, start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TicketPriceHistoryDTO> getLatestHistoryByTicket(Long ticketId) {
        return historyRepository.findLatestPriceChangeByTicketId(ticketId)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Long getPriceChangeCount(Long ticketId) {
        return historyRepository.countPriceChangesByTicketId(ticketId);
    }

    /**
     * 🔥 NOVO MÉTODO: Busca histórico por estratégia
     */
    @Transactional(readOnly = true)
    public List<TicketPriceHistoryDTO> getHistoryByStrategy(Long strategyId) {
        return historyRepository.findByStrategyId(strategyId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketPriceHistoryDTO> getHistoryByEventAndStrategy(Long eventId, Long strategyId) {
        return historyRepository.findByEventIdAndChangeType(eventId, String.valueOf(strategyId))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔥 NOVO MÉTODO: Busca tickets afetados por uma estratégia
     */
    @Transactional(readOnly = true)
    public List<TicketPriceChangeDTO> getTicketsAffectedByStrategy(Long strategyId) {
        List<TicketPriceHistory> histories = historyRepository.findByStrategyId(strategyId);

        return histories.stream()
                .map(h -> TicketPriceChangeDTO.builder()
                        .ticketId(h.getEventTicket().getId())
                        .ticketName(h.getEventTicket().getTicketName())
                        .category(h.getEventTicket().getCategory())
                        .oldPrice(h.getOldPrice())
                        .newPrice(h.getNewPrice())
                        .difference(h.getNewPrice().subtract(h.getOldPrice()))
                        .applied(true)
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS DE REGISTRO ====================

    @Transactional
    public TicketPriceHistoryDTO recordPriceChange(
            Long ticketId,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            String reason,
            String changeType,
            Long strategyId) {

        EventTicket ticket = eventTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        TicketPriceHistory history = new TicketPriceHistory();
        history.setEventTicket(ticket);
        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);
        history.setChangeReason(reason);
        history.setChangedAt(LocalDateTime.now());
        history.setChangeType(changeType);
        history.setStrategyId(strategyId);

        TicketPriceHistory savedHistory = historyRepository.save(history);
        log.info("Price change recorded for ticket {}: {} -> {} ({})",
                ticketId, oldPrice, newPrice, reason);

        return convertToDTO(savedHistory);
    }

    // ==================== MÉTODOS DE LIMPEZA ====================

    @Transactional
    public void deleteHistory(Long historyId) {
        if (historyRepository.existsById(historyId)) {
            historyRepository.deleteById(historyId);
            log.info("Price history deleted: {}", historyId);
        } else {
            throw new RuntimeException("Price history not found with id: " + historyId);
        }
    }

    // ==================== MÉTODO DE CONVERSÃO ====================

    /**
     * 🔥 MÉTODO QUE ESTAVA FALTANDO: Converte TicketPriceHistory para DTO
     */
    private TicketPriceHistoryDTO convertToDTO(TicketPriceHistory history) {
        if (history == null) return null;

        String strategyName = null;

        // Buscar nome da estratégia se existir
        if (history.getStrategyId() != null) {
            try {
                strategyName = pricingStrategyRepository.findById(history.getStrategyId())
                        .map(strategy -> strategy.getStrategyName())
                        .orElse(null);
            } catch (Exception e) {
                log.warn("Could not fetch strategy name for id: {}", history.getStrategyId());
            }
        }

        return TicketPriceHistoryDTO.builder()
                .id(history.getId())
                .eventTicketId(history.getEventTicket().getId())
                .eventTicketName(history.getEventTicket().getTicketName())
                .eventId(history.getEventTicket().getEvent().getId())
                .eventName(history.getEventTicket().getEvent().getName())
                .oldPrice(history.getOldPrice())
                .newPrice(history.getNewPrice())
                .changeReason(history.getChangeReason())
                .changedAt(history.getChangedAt())
                .changeType(history.getChangeType())
                .strategyId(history.getStrategyId())
                .strategyName(strategyName)
                .build();
    }
}