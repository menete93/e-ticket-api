// CancelEventRequestDTO.java
package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelEventRequestDTO {
    private String reason;
    private Boolean refundTickets = false;
}