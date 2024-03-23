package mate.academy.dto.user;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record RoleDto(@NotEmpty Set<String> roles) {
}
