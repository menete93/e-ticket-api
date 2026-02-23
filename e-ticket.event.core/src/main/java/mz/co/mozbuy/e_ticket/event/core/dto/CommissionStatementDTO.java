package mz.co.mozbuy.e_ticket.event.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommissionStatementDTO {

    private Long organizerId;
    private String organizerName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    private Integer totalSales;
    private Integer trialEventSales;
    private BigDecimal totalSalesAmount;
    private BigDecimal totalCommission;
    private BigDecimal netEarnings;
    private BigDecimal averageCommissionRate;

    private List<EventSummaryDTO> eventSummaries = new ArrayList<>();

    @Data
    public static class EventSummaryDTO {
        private Long eventId;
        private String eventName;
        private Integer ticketSales;
        private BigDecimal salesAmount;
        private BigDecimal commission;
        private BigDecimal payout;
        private Boolean isTrialEvent;
    }
}