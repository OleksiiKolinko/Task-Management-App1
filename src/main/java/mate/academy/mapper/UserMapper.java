package mate.academy.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.config.MapperConfig;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.model.Role;
import mate.academy.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface UserMapper {

    @Mapping(target = "roleDtos", source = "roles", qualifiedByName = "toRoleDto")
    UserResponseDtoWithRole toUserResponseWithRole(User user);

    @Named("toRoleDto")
    static Set<String> toRoleDto(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toSet());
    }
}
