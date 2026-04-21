package mz.co.mozbuy.e_ticket.event.core.dto.calculateDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationResponseDTO {
    private BigDecimal subtotal;
    private BigDecimal discountedPrice;
    private BigDecimal finalPrice;
    private BigDecimal totalSavings;
    private List<PriceBreakdownItemDTO> breakdown;
    private List<PriceBreakdownItemDTO> appliedStrategies;
}