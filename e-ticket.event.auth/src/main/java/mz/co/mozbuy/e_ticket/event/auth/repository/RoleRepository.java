package mz.co.mozbuy.e_ticket.event.auth.repository;




import mz.co.mozbuy.e_ticket.event.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);

    // 2️⃣ Depois, para cada role, carregar permissões
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r IN :roles")
    List<Role> findRolesWithPermissions(@Param("roles") Set<Role> roles);

    @Query("SELECT r FROM Role r WHERE r.name = :name")
    List<Role> findRolesByName(@Param("name") String name);

}