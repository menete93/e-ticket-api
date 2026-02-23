package mz.co.mozbuy.e_ticket.event.auth.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.auth.dto.OrganizerUpgradeRequest;
import mz.co.mozbuy.e_ticket.event.auth.dto.UserContext;
import mz.co.mozbuy.e_ticket.event.auth.dto.UserResponseDTO;
import mz.co.mozbuy.e_ticket.event.auth.exception.EmailAlreadyExistsException;
import mz.co.mozbuy.e_ticket.event.auth.exception.NotAnOrganizerException;
import mz.co.mozbuy.e_ticket.event.auth.exception.UserNotFoundException;
import mz.co.mozbuy.e_ticket.event.auth.exception.UsernameAlreadyExistsException;
import mz.co.mozbuy.e_ticket.event.auth.feignClient.service.UserUpgradeService;
import mz.co.mozbuy.e_ticket.event.auth.model.Role;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import mz.co.mozbuy.e_ticket.event.auth.repository.RoleRepository;
import mz.co.mozbuy.e_ticket.event.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final UserUpgradeService userUpgradeService;

    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public void testUserQuery(String username) {
        User user = userRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Usuário: " + user.getUsername());
        System.out.println("Roles:");
        user.getRoles().forEach(role -> {
            System.out.println("- " + role.getName());
            System.out.println("  Permissões:");
            role.getPermissions().forEach(p -> System.out.println("    * " + p.getName()));
        });
    }


    @Transactional
    public User registerUser(User user) {
        // Verificar existência
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException(user.getUsername());  // ✅ Exceção personalizada
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());  // ✅ Exceção personalizada
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(Collections.singleton(roleService.getDefaultRole())));
        }

        user.setCreatedBy("system");

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserContext getCurrentUserContext() {
        User user = getCurrentUser();
        return UserContext.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(
                        user.getRoles().stream()        // pega a lista de roles
                                .map(Role::getName)         // pega só o nome de cada role
                                .collect(Collectors.toList()) // transforma em List<String>
                )
                .build();


    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return (User) loadUserByUsername(username);
    }

    @Transactional
    public void updateLastLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public UserResponseDTO upgradeToOrganizer(OrganizerUpgradeRequest request) {
        User user = userUpgradeService.upgradeToOrganizer(request);
        return convertToDTO(user);
    }

    private UserResponseDTO convertToDTO(User user) {
        // Mapear User para UserResponseDTO
        return UserResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
//                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
//                .permissions(user.getRoles().stream()
//                        .flatMap(role -> role.getPermissions().stream())
//                        .map(Permission::getName)
//                        .distinct()
//                        .collect(Collectors.toList()))
                .isOrganizer(user.getIsOrganizer())
                .organizerReferenceId(user.getOrganizerReferenceId())
                .build();
    }


    // ✅ MÉTODO 1: Verificar se um usuário específico é organizador
    public boolean isUserOrganizer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return Boolean.TRUE.equals(user.getIsOrganizer());
    }

    // ✅ MÉTODO 2: Verificar se o usuário atual (do contexto) é organizador
    public boolean isCurrentUserOrganizer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;
            return Boolean.TRUE.equals(user.getIsOrganizer());
        }

        if (principal instanceof UserDetails) {
            // Se for Jwt ou outro tipo, extrair do contexto
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));
            return Boolean.TRUE.equals(user.getIsOrganizer());
        }

        return false;
    }

    // ✅ MÉTODO 3: Obter organizador por ID com validação
    public User getOrganizerById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!Boolean.TRUE.equals(user.getIsOrganizer())) {
            throw new NotAnOrganizerException(userId);
        }

        return user;
    }

}