package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RevenuePredictionDTO {
    private BigDecimal totalExpected;
    private BigDecimal totalSold;
    private BigDecimal totalRemaining;
    private Integer ticketCount;

    // Construtor vazio
    public RevenuePredictionDTO() {}

    // Construtor com todos os campos
    public RevenuePredictionDTO(BigDecimal totalExpected, BigDecimal totalSold,
                                BigDecimal totalRemaining, Integer ticketCount) {
        this.totalExpected = totalExpected;
        this.totalSold = totalSold;
        this.totalRemaining = totalRemaining;
        this.ticketCount = ticketCount;
    }
}