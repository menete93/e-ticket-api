//package mz.co.mozbuy.e_ticket.event.core.service;
//
//import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
//import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//
//@Component
//public class FixedPricingCalculator implements PricingCalculator {
//
//    @Override
//    public BigDecimal calculate(EventTicket ticket, PricingStrategy strategy) {
//        return strategy.getBasePrice();
//    }
//}