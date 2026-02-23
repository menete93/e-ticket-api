package mz.co.mozbuy.e_ticket.event.auth.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import mz.co.mozbuy.e_ticket.event.auth.model.Permission;
import mz.co.mozbuy.e_ticket.event.auth.model.Role;
import mz.co.mozbuy.e_ticket.event.auth.repository.PermissionRepository;
import mz.co.mozbuy.e_ticket.event.auth.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // ===== OPERAÇÕES BÁSICAS DE ROLE =====

    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role getDefaultRole() {
        return roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found. Please run data initialization."));
    }

    @Transactional(readOnly = true)
    public Role getAdminRole() {
        return roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found. Please run data initialization."));
    }

    @Transactional
    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("Role already exists: " + role.getName());
        }

        Role savedRole = roleRepository.save(role);
        log.info("Role created: {}", savedRole.getName());
        return savedRole;
    }

    @Transactional
    public Role updateRole(Long roleId, Role roleDetails) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        // Verifica se o nome já existe (para outro role)
        if (!existingRole.getName().equals(roleDetails.getName()) &&
                roleRepository.existsByName(roleDetails.getName())) {
            throw new RuntimeException("Role name already exists: " + roleDetails.getName());
        }

        existingRole.setName(roleDetails.getName());
        existingRole.setDescription(roleDetails.getDescription());

        Role updatedRole = roleRepository.save(existingRole);
        log.info("Role updated: {}", updatedRole.getName());
        return updatedRole;
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        // Verificar se existem usuários com este role (dependendo da sua regra de negócio)
        // if (!role.getUsers().isEmpty()) {
        //     throw new RuntimeException("Cannot delete role. There are users assigned to this role.");
        // }

        roleRepository.delete(role);
        log.info("Role deleted: {}", role.getName());
    }

    // ===== GERENCIAMENTO DE PERMISSÕES =====

    @Transactional
    public Role addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        // Evitar duplicatas
        if (role.getPermissions().stream().noneMatch(p -> p.getId().equals(permissionId))) {
            role.getPermissions().add(permission);
            Role updatedRole = roleRepository.save(role);
            log.info("Permission '{}' added to role '{}'", permission.getName(), role.getName());
            return updatedRole;
        }

        return role;
    }

    @Transactional
    public Role addPermissionToRoleByName(Long roleId, String permissionName) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));

        if (role.getPermissions().stream().noneMatch(p -> p.getName().equals(permissionName))) {
            role.getPermissions().add(permission);
            Role updatedRole = roleRepository.save(role);
            log.info("Permission '{}' added to role '{}'", permissionName, role.getName());
            return updatedRole;
        }

        return role;
    }

    @Transactional
    public Role removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        boolean removed = role.getPermissions().removeIf(permission -> permission.getId().equals(permissionId));

        if (removed) {
            Role updatedRole = roleRepository.save(role);
            log.info("Permission removed from role '{}'", role.getName());
            return updatedRole;
        }

        return role;
    }

    @Transactional
    public Role setRolePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        // Verificar se todas as permissions foram encontradas
        if (permissions.size() != permissionIds.size()) {
            Set<Long> foundIds = permissions.stream().map(Permission::getId).collect(Collectors.toSet());
            List<Long> missingIds = permissionIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new RuntimeException("Some permissions not found: " + missingIds);
        }

        role.setPermissions(new HashSet<>(permissions));
        Role updatedRole = roleRepository.save(role);
        log.info("Permissions updated for role '{}'. Total permissions: {}", role.getName(), permissions.size());

        return updatedRole;
    }

    @Transactional(readOnly = true)
    public List<Permission> getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        return new ArrayList<>(role.getPermissions());
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Long roleId, String permissionName) {
        return roleRepository.findById(roleId)
                .map(role -> role.getPermissions().stream()
                        .anyMatch(permission -> permission.getName().equals(permissionName)))
                .orElse(false);
    }

    // ===== OPERAÇÕES DE PERMISSION =====

    @Transactional(readOnly = true)
    public List<Permission> findAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Permission> findPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Permission> findPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    @Transactional
    public Permission createPermission(Permission permission) {
        if (permissionRepository.findByName(permission.getName()).isPresent()) {
            throw new RuntimeException("Permission already exists: " + permission.getName());
        }

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission created: {}", savedPermission.getName());
        return savedPermission;
    }

    @Transactional
    public Permission updatePermission(Long permissionId, Permission permissionDetails) {
        Permission existingPermission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        if (!existingPermission.getName().equals(permissionDetails.getName()) &&
                permissionRepository.findByName(permissionDetails.getName()).isPresent()) {
            throw new RuntimeException("Permission name already exists: " + permissionDetails.getName());
        }

        existingPermission.setName(permissionDetails.getName());
        existingPermission.setDescription(permissionDetails.getDescription());

        Permission updatedPermission = permissionRepository.save(existingPermission);
        log.info("Permission updated: {}", updatedPermission.getName());
        return updatedPermission;
    }

    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        // Remover esta permission de todos os roles
        List<Role> rolesWithPermission = roleRepository.findAll().stream()
                .filter(role -> role.getPermissions().contains(permission))
                .collect(Collectors.toList());

        for (Role role : rolesWithPermission) {
            role.getPermissions().remove(permission);
            roleRepository.save(role);
        }

        permissionRepository.delete(permission);
        log.info("Permission deleted: {}", permission.getName());
    }

    // ===== INICIALIZAÇÃO DE DADOS =====

    @Transactional
    public void initializeDefaultData() {
        initializePermissions();
        initializeRoles();
    }

    private void initializePermissions() {
        if (permissionRepository.count() == 0) {
            List<Permission> permissions = Arrays.asList(
                    createPermission("USER_READ", "Read user data"),
                    createPermission("USER_WRITE", "Write user data"),
                    createPermission("USER_DELETE", "Delete users"),
                    createPermission("ROLE_READ", "Read role data"),
                    createPermission("ROLE_WRITE", "Write role data"),
                    createPermission("ROLE_DELETE", "Delete roles"),
                    createPermission("PERMISSION_READ", "Read permissions"),
                    createPermission("PERMISSION_WRITE", "Write permissions"),
                    createPermission("PERMISSION_DELETE", "Delete permissions"),
                    createPermission("SESSION_MANAGE", "Manage user sessions"),
                    createPermission("SYSTEM_ADMIN", "Full system administration")
            );

            permissionRepository.saveAll(permissions);
            log.info("Default permissions initialized: {} permissions created", permissions.size());
        }
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            // Criar role USER
            Permission userRead = permissionRepository.findByName("USER_READ")
                    .orElseThrow(() -> new RuntimeException("USER_READ permission not found"));

            Role userRole = Role.builder()
                    .name("USER")
                    .description("Default user role")
                    .permissions(new HashSet<>(List.of(userRead)))
                    .build();

            // Criar role ADMIN
            List<Permission> adminPermissions = permissionRepository.findAll();

            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .description("Administrator role with full access")
                    .permissions(new HashSet<>(adminPermissions))
                    .build();

            // Criar role MODERATOR (exemplo)
            Permission userReadPerm = permissionRepository.findByName("USER_READ").orElseThrow();
            Permission userWritePerm = permissionRepository.findByName("USER_WRITE").orElseThrow();
            Permission sessionManagePerm = permissionRepository.findByName("SESSION_MANAGE").orElseThrow();

            Role moderatorRole = Role.builder()
                    .name("MODERATOR")
                    .description("Moderator role with user management capabilities")
                    .permissions(new HashSet<>(Arrays.asList(userReadPerm, userWritePerm, sessionManagePerm)))
                    .build();

            roleRepository.saveAll(Arrays.asList(userRole, adminRole, moderatorRole));
            log.info("Default roles initialized: USER, ADMIN, MODERATOR");
        }
    }

    private Permission createPermission(String name, String description) {
        return Permission.builder()
                .name(name)
                .description(description)
                .build();
    }

    // ===== VALIDAÇÕES E UTILITÁRIOS =====

    @Transactional(readOnly = true)
    public boolean roleExists(String name) {
        return roleRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public boolean permissionExists(String name) {
        return permissionRepository.findByName(name).isPresent();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRoleStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRoles", roleRepository.count());
        stats.put("totalPermissions", permissionRepository.count());

        List<Role> roles = roleRepository.findAll();
        Map<String, Integer> permissionsPerRole = roles.stream()
                .collect(Collectors.toMap(Role::getName, role -> role.getPermissions().size()));

        stats.put("permissionsPerRole", permissionsPerRole);
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Role> findRolesWithPermission(String permissionName) {
        return roleRepository.findAll().stream()
                .filter(role -> role.getPermissions().stream()
                        .anyMatch(permission -> permission.getName().equals(permissionName)))
                .collect(Collectors.toList());
    }
}