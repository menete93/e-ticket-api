package mz.co.mozbuy.e_ticket.event.auth.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    USER("USER"),
    ADMIN("ADMIN"),
    ORGANIZER("ORGANIZER"),
    VISITOR("VISITOR"),
    CONSULT("CONSULT");

    private final String value;

    RoleType(String value) {
        this.value = value;
    }

}
