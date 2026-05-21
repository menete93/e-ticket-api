package mz.co.mozbuy.e_ticket.event.core.mapper;

import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransactionDTO;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionMapper {

    public PaymentTransactionDTO toDTO(PaymentTransactionEntity entity) {
        if (entity == null) return null;

        return PaymentTransactionDTO.builder()
                .id(entity.getId())
                .transactionId(entity.getTransactionId())
                .externalId(entity.getExternalId())
                .saleId(entity.getSaleId())
                .eventId(entity.getEventId())
                .userId(entity.getUserId())
                .status(entity.getStatus())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .payerName(entity.getPayerName())
                .payerEmail(entity.getPayerEmail())
                .payerPhone(entity.getPayerPhone())
                .providerCheckoutId(entity.getProviderCheckoutId())
                .providerTransactionId(entity.getProviderTransactionId())
                .providerResultCode(entity.getProviderResultCode())
                .providerResultDesc(entity.getProviderResultDesc())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .state(entity.getState() != null ? entity.getState().name() : null)
                .build();
    }
}