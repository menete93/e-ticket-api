package mz.co.mozbuy.e_ticket.event.core.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequestDTO {


    @NotNull(message = "ticketId is required")
    private Long ticketId;
    @NotNull(message = "eventId is required")
    private  Long eventId;

    @NotNull(message = "Ticket category is required")
    private TicketCategory category;

    @NotBlank(message = "Ticket name is required")
    private String ticketName;

    @NotNull(message = "Total quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer totalQuantity;

    private BigDecimal price;

    private String description;
    private String benefits;
    private String salesStartDate;
    private String salesEndDate;
    private Integer maxTicketsPerUser = 10;
    private Boolean isActive = true;
}
