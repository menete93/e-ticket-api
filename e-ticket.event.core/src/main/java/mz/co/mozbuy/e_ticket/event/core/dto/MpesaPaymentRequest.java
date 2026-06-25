package mz.co.mozbuy.e_ticket.event.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MpesaPaymentRequest {
    @NotBlank(message = "Reservation code é obrigatório")
    private String reservationCode;  // ← antes transactionId

    @NotNull(message = "Sale ID é obrigatório")
    private Long saleId;

    @NotBlank(message = "Número de telefone é obrigatório")
    private String phoneNumber;
}