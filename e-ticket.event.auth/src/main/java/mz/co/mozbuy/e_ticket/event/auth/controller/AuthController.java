package mz.co.mozbuy.e_ticket.event.auth.controller;





import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.auth.dto.LoginRequest;
import mz.co.mozbuy.e_ticket.event.auth.dto.LoginResponse;
import mz.co.mozbuy.e_ticket.event.auth.dto.UserContext;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import mz.co.mozbuy.e_ticket.event.auth.service.AuthService;
import mz.co.mozbuy.e_ticket.event.auth.service.JwtService;
import mz.co.mozbuy.e_ticket.event.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User authenticatedUser = authService.authenticate(request.getUsername(), request.getPassword());

        // Gera o token JWT
        String jwtToken = jwtService.generateToken(authenticatedUser);

        // Atualiza último login
        userService.updateLastLogin(authenticatedUser.getUsername());

        UserContext userContext = UserContext.builder()
                .id(authenticatedUser.getId())
                .username(authenticatedUser.getUsername())
                .email(authenticatedUser.getEmail())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .role(authenticatedUser.getRole().getName())
                .build();

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
        User registeredUser = userService.registerUser(user);

        String jwtToken = jwtService.generateToken(registeredUser);

        UserContext userContext = UserContext.builder()
//                .id(registeredUser.getId())
                .username(registeredUser.getUsername())
                .email(registeredUser.getEmail())
                .firstName(registeredUser.getFirstName())
                .lastName(registeredUser.getLastName())
                .role(registeredUser.getRole().getName())
                .build();

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
}