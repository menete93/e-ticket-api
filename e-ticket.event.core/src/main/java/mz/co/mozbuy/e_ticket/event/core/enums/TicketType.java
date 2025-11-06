package mz.co.mozbuy.e_ticket.event.core.enums;

public enum TicketType {

    V_VIP(0, "Very VIP"),
    VIP(1, "VIP"),
    NORMAL(2, "Normal"),
    INVITE(3, "Convite");

    private final int codigo;
    private final String descricao;

    TicketType(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    // Buscar o enum pelo código numérico
    public static TicketType fromCodigo(int codigo) {
        for (TicketType tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}
