//package mz.co.mozbuy.e_ticket.event.core.service;
//
//import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
//import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
//
//@Component
//public class TimeBasedPricingCalculator implements PricingCalculator {
//
//    @Override
//    public BigDecimal calculate(EventTicket ticket, PricingStrategy strategy) {
//
//        long daysToEvent = ChronoUnit.DAYS.between(
//                LocalDate.now(),
//                ticket.getEvent().getEventDate()
//        );
//
//        if (daysToEvent <= strategy.getTimeBasedIncreaseDays()) {
//            BigDecimal increase = strategy.getBasePrice()
//                    .multiply(strategy.getTimeBasedIncreasePercentage())
//                    .divide(BigDecimal.valueOf(100));
//
//            return strategy.getBasePrice().add(increase);
//        }
//
//        return strategy.getBasePrice();
//    }
//}