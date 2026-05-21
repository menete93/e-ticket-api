package mz.co.mozbuy.e_ticket.event.core.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpesaPaymentRequestDTO {

    @NotNull(message = "saleId é obrigatório")
    private Long saleId;

    @NotNull(message = "eventId é obrigatório")
    private Long eventId;

    private Long userId;

    @NotNull(message = "amount é obrigatório")
    @DecimalMin(value = "1.00", message = "Valor mínimo é 1.00 MT")
    private BigDecimal amount;

    @NotBlank(message = "phoneNumber é obrigatório")
    @Pattern(regexp = "^8[2-5][0-9]{7}$", message = "Número de telefone inválido")
    private String phoneNumber;

    @NotBlank(message = "payerName é obrigatório")
    private String payerName;

    @NotBlank(message = "payerEmail é obrigatório")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Email inválido")
    private String payerEmail;

    private String description;
    private String returnUrl;
    private String cancelUrl;
}
