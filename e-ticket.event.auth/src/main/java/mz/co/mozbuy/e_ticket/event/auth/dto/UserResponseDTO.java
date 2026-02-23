package mz.co.mozbuy.e_ticket.event.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@Builder
public class UserResponseDTO {

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isOrganizer;
    private String organizerReferenceId; // UUID do perfil no Ticket Service


}
