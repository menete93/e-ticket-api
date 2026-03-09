package mz.co.mozbuy.e_ticket.event.core.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkStrategyAssignmentDTO {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotEmpty(message = "At least one strategy assignment is required")
    private List<StrategyAssignment> strategyAssignments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrategyAssignment {

        @NotNull(message = "Strategy ID is required")
        private String strategyId; // LOYALTY_DISCOUNT, TIERED_PRICING, etc.

        @NotEmpty(message = "At least one target category is required")
        private List<TicketCategory> targetCategories;
    }
}