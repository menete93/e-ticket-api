package mz.co.mozbuy.e_ticket.event.core.dto.calculateDto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequestDTO {
    private Long userId;
    private Long eventId;
    private String email;
    private String couponCode;
    private Map<Long, Integer> ticketQuantities; // ticketId -> quantidade
}



