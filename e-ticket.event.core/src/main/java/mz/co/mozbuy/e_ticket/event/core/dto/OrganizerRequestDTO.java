package mz.co.mozbuy.e_ticket.event.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrganizerRequestDTO {


    @NotNull(message = "O utilizador é obrigatório")
    private Long userId;

    @NotBlank(message = "O nome do organizador é obrigatório")
    private String name;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;


    private String phoneNumber;

    private String companyName;

    @NotBlank(message = "NUIT é obrigatório")
    @Pattern(regexp = "\\d{9}", message = "NUIT deve ter 9 dígitos")
    private String nuit;

    // Configuração de pricing
    private BigDecimal commissionRate;

    private BigDecimal flatFeePerTicket;

    private Integer trialEventsRemaining;
}