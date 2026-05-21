package mz.co.mozbuy.e_ticket.event.core.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpesaPaymentRequest {

    @NotNull(message = "saleId é obrigatório")
    private Long saleId;

    @NotNull(message = "eventId é obrigatório")
    private Long eventId;

    @NotBlank(message = "transactionId é obrigatório")
    private String transactionId;

    @NotNull(message = "valor é obrigatório")
    private BigDecimal amount;

    @NotBlank(message = "phoneNumber é obrigatório")
    @Pattern(regexp = "^8[2-7][0-9]{7}$", message = "Número de telefone inválido")
    private String phoneNumber;

    private Long userId;
    private String payerName;
    private String payerEmail;
}