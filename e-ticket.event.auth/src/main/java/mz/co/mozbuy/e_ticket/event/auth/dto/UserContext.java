package mz.co.mozbuy.e_ticket.event.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String role;
}