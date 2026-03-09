//package mz.co.mozbuy.e_ticket.event.core.service;
//
//import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
//import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//
//@Component
//public class GroupDiscountCalculator implements PricingCalculator {
//
//    @Override
//    public BigDecimal calculate(EventTicket ticket, PricingStrategy strategy) {
//
//        int quantity = ticket.getReservedQuantity(); // exemplo
//
//        if (quantity >= strategy.getGroupSizeThreshold()) {
//            BigDecimal discount = strategy.getBasePrice()
//                    .multiply(strategy.getGroupDiscountPercentage())
//                    .divide(BigDecimal.valueOf(100));
//
//            return strategy.getBasePrice().subtract(discount);
//        }
//
//        return strategy.getBasePrice();
//    }
//}