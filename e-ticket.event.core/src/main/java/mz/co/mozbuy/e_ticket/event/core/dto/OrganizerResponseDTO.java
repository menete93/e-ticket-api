
package mz.co.mozbuy.e_ticket.event.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrganizerResponseDTO {

    @NotNull(message = "O utilizador é obrigatório")
    private Long id;

    @NotBlank(message = "a referencia do organizador é obrigatório")
    private String organizerReferenceId; // UUID do perfil no Ticket Service

    @NotBlank(message = "O nome do organizador é obrigatório")
    private String name;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
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

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}