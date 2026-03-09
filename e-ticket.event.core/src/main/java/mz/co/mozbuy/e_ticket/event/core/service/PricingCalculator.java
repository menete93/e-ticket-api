package mz.co.mozbuy.e_ticket.event.core.service;

import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;

import java.math.BigDecimal;

public interface PricingCalculator {
    BigDecimal calculate(EventTicket ticket, PricingStrategy strategy);
}