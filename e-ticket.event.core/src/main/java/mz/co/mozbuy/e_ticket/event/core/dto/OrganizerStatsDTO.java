package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrganizerStatsDTO {

    private Long organizerId;
    private String organizerName;

    private Integer totalEventsCreated;
    private Integer totalTicketsSold;
    private BigDecimal totalEarnings;
    private BigDecimal totalCommissionPaid;
    private BigDecimal netEarnings;
    private BigDecimal accountBalance;

    private Integer trialEventsRemaining;
    private Integer trialEventsUsed;

    // Métodos auxiliares
    public BigDecimal getAverageTicketPrice() {
        if (totalTicketsSold == null || totalTicketsSold == 0) {
            return BigDecimal.ZERO;
        }
        return totalEarnings.divide(BigDecimal.valueOf(totalTicketsSold), 2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getAverageCommissionPerTicket() {
        if (totalTicketsSold == null || totalTicketsSold == 0) {
            return BigDecimal.ZERO;
        }
        return totalCommissionPaid.divide(BigDecimal.valueOf(totalTicketsSold), 2, java.math.RoundingMode.HALF_UP);
    }
}