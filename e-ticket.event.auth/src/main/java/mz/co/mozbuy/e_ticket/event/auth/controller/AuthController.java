package mz.co.mozbuy.e_ticket.event.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.auth.dto.*;
import mz.co.mozbuy.e_ticket.event.auth.model.Permission;
import mz.co.mozbuy.e_ticket.event.auth.model.Role;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import mz.co.mozbuy.e_ticket.event.auth.service.AuthService;
import mz.co.mozbuy.e_ticket.event.auth.service.JwtService;
import mz.co.mozbuy.e_ticket.event.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1️⃣ Autentica o usuário
        User authenticatedUser = authService.authenticate(request.getUsername(), request.getPassword());

        // 2️⃣ Gera o token JWT
        String jwtToken = jwtService.generateToken(authenticatedUser);

        // 3️⃣ Atualiza último login
        userService.updateLastLogin(authenticatedUser.getUsername());

        // 4️⃣ Extrai nomes das roles
        List<String> roleNames = authenticatedUser.getRoles().stream()
                .map(Role::getName)
                .toList();

        // 5️⃣ Extrai authorities (roles + permissões)
        List<String> authorities = authenticatedUser.getRoles().stream()
                .flatMap(role -> {
                    List<String> auths = new ArrayList<>();
                    auths.add("ROLE_" + role.getName()); // role com prefixo ROLE_
                    auths.addAll(role.getPermissions().stream()
                            .map(Permission::getName)
                            .toList());
                    return auths.stream();
                })
                .toList();

        // 6️⃣ Monta UserContext
        UserContext userContext = UserContext.builder()
                .id(authenticatedUser.getId())
                .username(authenticatedUser.getUsername())
                .email(authenticatedUser.getEmail())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .state(authenticatedUser.getState())
                .isOrganizer(authenticatedUser.getIsOrganizer())
                .referenceId(authenticatedUser.getOrganizerReferenceId())
                .roles(roleNames)         // lista de roles
                .authorities(authorities) // lista de roles + permissões
                .build();

        // 7️⃣ Monta LoginResponse
        LoginResponse response = LoginResponse.builder()
                .token(jwtToken)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userContext)
                .build();

        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody User user) {
        // 1️⃣ Registra o usuário
        User registeredUser = userService.registerUser(user);

        // 2️⃣ Gera o token JWT
        String jwtToken = jwtService.generateToken(registeredUser);

        // 3️⃣ Extrai nomes das roles
        List<String> roleNames = registeredUser.getRoles().stream()
                .map(Role::getName)
                .toList();

        // 4️⃣ Extrai authorities (roles + permissões)
        List<String> authorities = registeredUser.getRoles().stream()
                .flatMap(role -> {
                    List<String> auths = new ArrayList<>();
                    auths.add("ROLE_" + role.getName()); // role com prefixo ROLE_
                    auths.addAll(role.getPermissions().stream()
                            .map(Permission::getName)
                            .toList());
                    return auths.stream();
                })
                .toList();

        // 5️⃣ Monta UserContext
        UserContext userContext = UserContext.builder()
                .id(registeredUser.getId())
                .username(registeredUser.getUsername())
                .email(registeredUser.getEmail())
                .firstName(registeredUser.getFirstName())
                .lastName(registeredUser.getLastName())
                .roles(roleNames)         // lista de roles
                .authorities(authorities) // lista de roles + permissões
                .build();

        // 6️⃣ Monta LoginResponse
        LoginResponse response = LoginResponse.builder()
                .token(jwtToken)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userContext)
                .build();

        return ResponseEntity.ok(response);
    }


    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(false);
        }

        String token = authHeader.substring(7);
        boolean isValid = jwtService.isTokenValid(token);

        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            return ResponseEntity.ok(userService.getCurrentUserContext());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
    }



    @PostMapping("/upgrade-to-organizer")
//    @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasRole('ADMIN') and #userId == authentication.principal.id")

    public ResponseEntity<UserResponseDTO> upgradeToOrganizer(
             @RequestBody OrganizerUpgradeRequest request) {

        UserResponseDTO userDTO = userService.upgradeToOrganizer(request);
        return ResponseEntity.ok(userDTO);
    }

}