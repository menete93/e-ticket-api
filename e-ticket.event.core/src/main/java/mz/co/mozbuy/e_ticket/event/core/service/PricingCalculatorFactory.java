//package mz.co.mozbuy.e_ticket.event.core.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class PricingCalculatorFactory {
//
//    private final FixedPricingCalculator fixed;
//    private final TimeBasedPricingCalculator timeBased;
//    private final GroupDiscountCalculator group;
//
//    public PricingCalculator getCalculator(String type) {
//
//        return switch (type) {
//            case "FIXED" -> fixed;
//            case "TIME_BASED" -> timeBased;
//            case "GROUP_DISCOUNT" -> group;
//            default -> throw new IllegalArgumentException("Unsupported strategy type");
//        };
//    }
//}
