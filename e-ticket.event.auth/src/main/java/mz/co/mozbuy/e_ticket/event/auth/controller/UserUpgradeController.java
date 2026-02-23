package mz.co.mozbuy.e_ticket.event.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.auth.dto.OrganizerUpgradeRequest;
import mz.co.mozbuy.e_ticket.event.auth.dto.UserResponseDTO;
import mz.co.mozbuy.e_ticket.event.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserUpgradeController {

    private final UserService userService;

    @PostMapping("/upgrade-to-organizer")
//    @PreAuthorize("hasRole('ADMIN')")
//    @PreAuthorize("hasRole('ADMIN') and #userId == authentication.principal.id")

    public ResponseEntity<UserResponseDTO> upgradeToOrganizer(
            @Valid @RequestBody OrganizerUpgradeRequest request) {

        UserResponseDTO userDTO = userService.upgradeToOrganizer(request);
        return ResponseEntity.ok(userDTO);
    }
}