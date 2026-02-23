package mz.co.mozbuy.e_ticket.event.auth.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.common.audit.DomainEntity;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends DomainEntity<Long> {


    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role(String name) {
        this.name = name;
        this.permissions = new HashSet<>();
    }

}
