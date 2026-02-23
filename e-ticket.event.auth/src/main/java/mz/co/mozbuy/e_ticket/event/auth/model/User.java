package mz.co.mozbuy.e_ticket.event.auth.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.common.audit.AuditableEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends AuditableEntity<Long, String> implements UserDetails {


    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    @Email(message = "Formato do email invalido")
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Column(name = "is_organizer")
    private Boolean isOrganizer = false;

    @Column(name = "organizer_reference_id")
    private String organizerReferenceId; // UUID do perfil no Ticket Service

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserSession> sessions = new HashSet<>();



    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Para cada role do usuário
        for (Role role : roles) {
            // Adiciona a role com prefixo ROLE_
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Adiciona todas as permissões do role
            authorities.addAll(
                    role.getPermissions().stream()
                            .map(p -> new SimpleGrantedAuthority(p.getName()))
                            .toList()
            );
        }

        return authorities;
    }


    public boolean isOrganizerWithRole() {
        // Verifica tanto o campo quanto a role
        boolean hasOrganizerField = Boolean.TRUE.equals(this.isOrganizer);
        boolean hasOrganizerRole = this.getRoles().stream()
                .anyMatch(role ->
                        "ROLE_ORGANIZER".equals(role.getName()) ||
                                "ORGANIZER".equals(role.getName())
                );

        return hasOrganizerField && hasOrganizerRole;
    }


    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getUsefFullName() {
        return firstName + " " + lastName;
    }

}
