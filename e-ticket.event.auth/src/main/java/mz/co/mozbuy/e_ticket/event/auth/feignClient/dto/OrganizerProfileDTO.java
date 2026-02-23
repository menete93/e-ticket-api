package mz.co.mozbuy.e_ticket.event.auth.feignClient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerProfileDTO {


    private Long userId;
    private String referenceId;
    private String name;
    private String email;
    private String phoneNumber;
    private String companyName;
    private String nuit;

    // Configuração de pricing
    private BigDecimal commissionRate;
    private BigDecimal flatFeePerTicket;
    private Integer trialEventsRemaining;
    private Integer trialUsedCount;

    // Status
    private LifeCycleState lifeCycleState;

    // Estatísticas
    private BigDecimal accountBalance;
    private BigDecimal totalEarnings;
    private BigDecimal totalCommissionPaid;
    private Integer totalTicketsSold;
    private Integer totalEventsCreated;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
