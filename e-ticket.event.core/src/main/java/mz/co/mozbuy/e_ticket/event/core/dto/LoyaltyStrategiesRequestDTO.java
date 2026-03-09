package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyStrategiesRequestDTO {
    private boolean includePlatinum;
    private boolean includeGold;
    private boolean includeSilver;
    private boolean includeBronze;
    private boolean includeFirstBuyer;
    private boolean includeVolumeBased;
    private boolean includeRepeatBuyer;
    private boolean includeHighSpender;

    // Métodos factory
    public static LoyaltyStrategiesRequestDTO all() {
        return LoyaltyStrategiesRequestDTO.builder()
                .includePlatinum(true)
                .includeGold(true)
                .includeSilver(true)
                .includeBronze(true)
                .includeFirstBuyer(true)
                .includeVolumeBased(true)
                .includeRepeatBuyer(true)
                .includeHighSpender(true)
                .build();
    }

    public static LoyaltyStrategiesRequestDTO onlyTiers() {
        return LoyaltyStrategiesRequestDTO.builder()
                .includePlatinum(true)
                .includeGold(true)
                .includeSilver(true)
                .includeBronze(true)
                .includeFirstBuyer(false)
                .includeVolumeBased(false)
                .includeRepeatBuyer(false)
                .includeHighSpender(false)
                .build();
    }
}