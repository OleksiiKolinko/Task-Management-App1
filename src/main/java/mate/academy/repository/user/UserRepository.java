package mate.academy.repository.user;

import java.util.Optional;
import java.util.Set;
import mate.academy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Set<User> findByRolesId(Long roleId);
}
