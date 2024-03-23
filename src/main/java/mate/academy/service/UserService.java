package mate.academy.service;

import mate.academy.dto.user.RoleDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.exception.RegistrationException;

public interface UserService {
    UserResponseDtoWithRole register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserResponseDtoWithRole updateRole(Long userId, RoleDto roleDto);

    UserResponseDtoWithRole getProfileInfo(Long userId);

    UserResponseDtoWithRole updateProfileInfo(Long userId, UserRegistrationRequestDto requestDto)
            throws RegistrationException;
}
