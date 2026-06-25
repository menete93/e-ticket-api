package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.DomainEntity;
import org.yaml.snakeyaml.events.Event;

@Getter
@Setter
@Entity
@Table(name = "payment_method")
public class PaymentMethodEntity extends DomainEntity<Long>{


    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;           // "MPESA", "VISA", "BANK_TRANSFER"

    @Column(name = "name", nullable = false, length = 100)
    private String name;           // "M-Pesa", "Cartão Visa", "Transferência"

    @Column(name = "flow_type", nullable = false, length = 20)
    private String flowType;       // "SYNCHRONOUS" ou "ASYNCHRONOUS"

    @Column(name = "icon_url", length = 255)
    private String iconUrl;        // Caminho do ícone

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private PaymentProviderConfigEntity provider;  // Configurações específicas
}