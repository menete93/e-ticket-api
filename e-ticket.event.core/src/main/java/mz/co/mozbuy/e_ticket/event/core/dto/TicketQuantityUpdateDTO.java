// TicketQuantityUpdateDTO.java
package mz.co.mozbuy.e_ticket.event.core.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketQuantityUpdateDTO {

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Total quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer totalQuantity;

    private String changeReason;
}