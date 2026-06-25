package mz.co.mozbuy.e_ticket.event.core.enums;


public enum FlowType {
    SYNCHRONOUS,    // Resposta imediata (M-Pesa, Cartão)
    ASYNCHRONOUS    // Requer callback (Transferência, Boleto)
}