package mz.co.mozbuy.e_ticket.event.auth.feignClient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
//DTO DE RESPOSTA DO TIICKET PARA AUTH
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizerProfileResponse {


    private String organizerReferenceId; // UUID do perfil criado
    private String Id; // Id do Organizador criado
    private String organizerId; // UUID do perfil criado
    private Long userId;
    private String companyName;
    private LocalDateTime createdAt;
}
