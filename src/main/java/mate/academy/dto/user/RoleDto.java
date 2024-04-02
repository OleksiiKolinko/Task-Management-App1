package mate.academy.dto.user;

import java.util.Set;
import mate.academy.annotation.ValueOfEnum;
import mate.academy.model.Role;

public record RoleDto(Set<@ValueOfEnum(enumClass = Role.RoleName.class, message =
        "Wrong roleName. There are four roles: ROLE_ADMIN, ROLE_MANAGER, "
                + "ROLE_USER and WITHOUT_ROLE") String> roles) {
}
