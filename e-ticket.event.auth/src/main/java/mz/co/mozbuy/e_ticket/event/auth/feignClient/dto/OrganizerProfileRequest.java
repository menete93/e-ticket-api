package mz.co.mozbuy.e_ticket.event.auth.feignClient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO QUE VAI AO TICKET SERVICE
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerProfileRequest {

    @NotBlank(message = "userId name is required")
    private Long userId;

    @NotBlank(message = "Organizer name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    private String companyName;

    private String nuit; // NUIT, CNPJ, etc.

}

