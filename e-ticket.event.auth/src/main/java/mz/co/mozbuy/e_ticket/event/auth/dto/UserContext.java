package mz.co.mozbuy.e_ticket.event.auth.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isOrganizer;
    private String referenceId; // UUID do perfil no Ticket Service
    private LifeCycleState lifecycleStatus;
    private List<String> roles;
    private List<String> authorities;
//    private List<String> session;
}