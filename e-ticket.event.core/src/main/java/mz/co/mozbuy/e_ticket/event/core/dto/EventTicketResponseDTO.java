package mz.co.mozbuy.e_ticket.event.core.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventTicketResponseDTO {
    private Long id;
    private TicketCategory category;
    private String ticketName;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer soldQuantity;
    private BigDecimal price;
    private String description;
    private String benefits;
    private LocalDateTime salesStartDate;
    private LocalDateTime salesEndDate;
    private Integer maxTicketsPerUser;
    private Boolean isActive;
    private Boolean isAvailable;
    private Boolean isSalesPeriodActive;
    private BigDecimal totalRevenue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}