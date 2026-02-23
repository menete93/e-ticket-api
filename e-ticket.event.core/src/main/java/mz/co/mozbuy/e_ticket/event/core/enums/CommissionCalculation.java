package mz.co.mozbuy.e_ticket.event.core.enums;
import java.math.BigDecimal;

public record CommissionCalculation(
        BigDecimal rate,      // Taxa de comissão (ex: 0.05 para 5%)
        BigDecimal amount,    // Valor da comissão em MZN
        BigDecimal payout,    // Valor que o organizador recebe
        boolean isTrial       // Se é um evento trial (comissão 0%)
) {}