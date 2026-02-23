package mz.co.mozbuy.e_ticket.event.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.auth.model.Permission;
import mz.co.mozbuy.e_ticket.event.auth.model.Role;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import mz.co.mozbuy.e_ticket.event.auth.repository.PermissionRepository;
import mz.co.mozbuy.e_ticket.event.auth.repository.RoleRepository;
import mz.co.mozbuy.e_ticket.event.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner initData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {

        return args -> {
            if (userRepository.count() > 0) {
                log.info("Dados já inicializados. Pulando inicialização.");
                return;
            }

            log.info("Inicializando dados do sistema...");

            // 1️⃣ Criar Permissões
            List<Permission> permissions = Arrays.asList(
                    createPermissionIfNotFound("USER_READ", "Ler informações de usuário"),
                    createPermissionIfNotFound("USER_WRITE", "Criar/editar usuários"),
                    createPermissionIfNotFound("USER_DELETE", "Deletar usuários"),
                    createPermissionIfNotFound("ROLE_READ", "Ler funções"),
                    createPermissionIfNotFound("ROLE_WRITE", "Criar/editar funções"),
                    createPermissionIfNotFound("EVENT_READ", "Ler eventos"),
                    createPermissionIfNotFound("EVENT_WRITE", "Criar/editar eventos"),
                    createPermissionIfNotFound("EVENT_DELETE", "Deletar eventos"),
                    createPermissionIfNotFound("TICKET_READ", "Ler tickets"),
                    createPermissionIfNotFound("TICKET_WRITE", "Criar/editar tickets"),
                    createPermissionIfNotFound("ADMIN_ACCESS", "Acesso administrativo completo")
            );
            permissionRepository.saveAll(permissions);
            log.info("Permissões criadas: {}", permissions.size());

            // 2️⃣ Criar Roles
            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .description("Administrador do sistema")
                    .permissions(new HashSet<>(permissions)) // Admin tem todas as permissões
                    .build();

            Role userRole = Role.builder()
                    .name("USER")
                    .description("Usuário normal do sistema")
                    .permissions(new HashSet<>(Arrays.asList(
                            findPermissionByName(permissions, "USER_READ"),
                            findPermissionByName(permissions, "EVENT_READ"),
                            findPermissionByName(permissions, "TICKET_READ"),
                            findPermissionByName(permissions, "TICKET_WRITE")
                    )))
                    .build();

            roleRepository.saveAll(Arrays.asList(adminRole, userRole));
            log.info("Roles criadas e permissões associadas: ADMIN, USER");

            // 3️⃣ Criar Usuário Admin
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@eticket.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Administrador")
                    .lastName("do Sistema")
                    .roles(new HashSet<>(Collections.singletonList(adminRole))) // ✅ ADMIN
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            adminUser.setCreatedBy("system");

            // 4️⃣ Criar Usuário Normal
            User normalUser = User.builder()
                    .username("joao.silva")
                    .email("joao.silva@email.com")
                    .password(passwordEncoder.encode("password123"))
                    .firstName("João")
                    .lastName("Silva")
                    .roles(new HashSet<>(Collections.singletonList(userRole))) // ✅ USER
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            normalUser.setCreatedBy("system");

            // 5️⃣ Salvar usuários
            userRepository.saveAll(Arrays.asList(adminUser, normalUser));
            log.info("Usuários criados: admin, joao.silva");

            log.info("✅ Dados inicializados com sucesso!");
        };
    }

    private Permission createPermissionIfNotFound(String name, String description) {
        return Permission.builder()
                .name(name)
                .description(description)
                .build();
    }

    private Permission findPermissionByName(List<Permission> permissions, String name) {
        return permissions.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Permission not found: " + name));
    }
}
