package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Builder;
import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.model.CustomerPurchaseHistory;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class LoyaltyReportDTO {
    private Long platinumCustomers;
    private Long goldCustomers;
    private Long silverCustomers;
    private Long bronzeCustomers;
    private Long newCustomers;
    private Long totalCustomers;
    private Double averageCustomerSpend;
    private double platinumPercentage;
    private double goldPercentage;
    private double silverPercentage;
    private double bronzePercentage;
    private List<CustomerPurchaseHistory> topCustomers;




}