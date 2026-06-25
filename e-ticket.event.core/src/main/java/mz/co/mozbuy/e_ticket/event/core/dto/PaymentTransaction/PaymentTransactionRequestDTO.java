package mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionRequestDTO {

    private String reservationCode;  // ← antes transactionId

    @NotNull(message = "Sale ID é obrigatório")
    private Long saleId;

    @NotNull(message = "Event ID é obrigatório")
    private Long eventId;

    private Long userId;

    @NotNull(message = "Amount é obrigatório")
    private BigDecimal amount;

    @NotBlank(message = "Payment method code é obrigatório")
    private String paymentMethodCode;

    private String currency;


    private Integer quantity;


}