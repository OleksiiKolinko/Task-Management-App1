package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDtoWithRole toUserResponseWithRole(User user);
}
