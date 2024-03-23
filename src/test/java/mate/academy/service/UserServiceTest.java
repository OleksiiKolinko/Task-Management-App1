package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.user.RoleDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.mapper.UserMapper;
import mate.academy.model.Role;
import mate.academy.model.User;
import mate.academy.repository.role.RoleRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final int ONE = 1;
    private static final Long ONE_ID = 1L;
    private static final Long THREE_ID = 3L;
    private static final String USERNAME = "username";
    private static final String UPDATE_USERNAME = "updateUsername";
    private static final String EMAIL = "email@example.com";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PASSWORD = "password";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String WITHOUT_ROLE = "WITHOUT_ROLE";
    private static final Long TWO_ID = 2L;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Verify register() method works")
    public void register_Valid_ReturnUserResponseDtoWithRole() {
        final UserResponseDtoWithRole expect = getRegisteredUserResponseDtoWithRole();
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(getDefaultRole()));
        when(passwordEncoder.encode(anyString())).thenReturn(PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(getRegisterdUser());
        when(userMapper.toUserResponseWithRole(any(User.class)))
                .thenReturn(getUserResponseDtoWithRole());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getRegisterdUser()));
        final UserResponseDtoWithRole actual = userService.register(
                new UserRegistrationRequestDto(USERNAME, PASSWORD, PASSWORD,
                        EMAIL, FIRST_NAME, LAST_NAME));
        assertThat(actual).isEqualTo(expect);
        verify(roleRepository, times(ONE)).findById(anyLong());
        verify(passwordEncoder, times(ONE)).encode(anyString());
        verify(userRepository, times(ONE)).save(any(User.class));
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(userRepository, times(ONE)).findByEmail(anyString());
        verify(userRepository, times(ONE)).findByUsername(anyString());
        verify(userMapper, times(ONE)).toUserResponseWithRole(any(User.class));
        verifyNoMoreInteractions(roleRepository, passwordEncoder, userRepository, userMapper);
    }

    @Test
    @DisplayName("Verify updateRole() method works")
    public void updateRole_Valid_ReturnUserResponseDtoWithRole() {
        final UserResponseDtoWithRole expect = getUserResponseDtoWithRole();
        when(roleRepository.findByName(Role.RoleName.valueOf(ROLE_USER)))
                .thenReturn(getRoles().stream().findFirst());
        when(roleRepository.findByName(Role.RoleName.valueOf(ROLE_MANAGER)))
                .thenReturn(Optional.of(getRoleManager()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getRegisterdUser()));
        when(userRepository.save(any(User.class))).thenReturn(getAssignee());
        when(userMapper.toUserResponseWithRole(any(User.class)))
                .thenReturn(getUserResponseDtoWithRole());
        final UserResponseDtoWithRole actual = userService.updateRole(TWO_ID,
                new RoleDto(Set.of(ROLE_MANAGER)));
        assertThat(actual).isEqualTo(expect);
        verify(roleRepository, times(ONE)).findByName(Role.RoleName.valueOf(ROLE_USER));
        verify(roleRepository, times(ONE)).findByName(Role.RoleName.valueOf(ROLE_MANAGER));
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(userRepository, times(ONE)).save(any(User.class));
        verify(userMapper, times(ONE)).toUserResponseWithRole(any(User.class));
        verifyNoMoreInteractions(roleRepository, userRepository, userMapper);
    }

    @Test
    @DisplayName("Verify updateRole() method works")
    public void getProfileInfo_Valid_ReturnUserResponseDtoWithRole() {
        final UserResponseDtoWithRole expect = getUserResponseDtoWithRole();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getAssignee()));
        when(userMapper.toUserResponseWithRole(any(User.class)))
                .thenReturn(getUserResponseDtoWithRole());
        final UserResponseDtoWithRole actual = userService.getProfileInfo(TWO_ID);
        assertThat(actual).isEqualTo(expect);
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(userMapper, times(ONE)).toUserResponseWithRole(any(User.class));
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Verify updateProfileInfo() method works")
    public void updateProfileInfo_Valid_ReturnUserResponseDtoWithRole() {
        final UserResponseDtoWithRole expect = getUpdatedUserResponseDtoWithRole();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getAssignee()));
        when(userRepository.save(any(User.class))).thenReturn(getUpdatedUser());
        when(userMapper.toUserResponseWithRole(any(User.class)))
                .thenReturn(getUpdatedUserResponseDtoWithRole());
        final UserResponseDtoWithRole actual = userService.updateProfileInfo(TWO_ID,
                new UserRegistrationRequestDto(UPDATE_USERNAME, PASSWORD, PASSWORD,
                        EMAIL, FIRST_NAME, LAST_NAME));
        assertThat(actual).isEqualTo(expect);
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(userRepository, times(ONE)).save(any(User.class));
        verify(userMapper, times(ONE)).toUserResponseWithRole(any(User.class));
        verify(userRepository, times(ONE)).findByEmail(anyString());
        verify(userRepository, times(ONE)).findByUsername(anyString());
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    private User getUpdatedUser() {
        final User user = new User();
        user.setId(TWO_ID);
        user.setUsername(UPDATE_USERNAME);
        user.setPassword(PASSWORD);
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setRoles(getRoles());
        return user;
    }

    private UserResponseDtoWithRole getUpdatedUserResponseDtoWithRole() {
        final UserResponseDtoWithRole userResponseDto = new UserResponseDtoWithRole();
        userResponseDto.setId(TWO_ID);
        userResponseDto.setUsername(UPDATE_USERNAME);
        userResponseDto.setEmail(EMAIL);
        userResponseDto.setFirstName(FIRST_NAME);
        userResponseDto.setLastName(LAST_NAME);
        Set<String> roleDtos = new HashSet<>();
        roleDtos.add(ROLE_MANAGER);
        userResponseDto.setRoleDtos(roleDtos);
        return userResponseDto;
    }

    private User getRegisterdUser() {
        final User user = new User();
        user.setId(TWO_ID);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setRoles(Set.of(getDefaultRole()));
        return user;
    }

    private UserResponseDtoWithRole getRegisteredUserResponseDtoWithRole() {
        final UserResponseDtoWithRole userResponseDto = new UserResponseDtoWithRole();
        userResponseDto.setId(TWO_ID);
        userResponseDto.setUsername(USERNAME);
        userResponseDto.setEmail(EMAIL);
        userResponseDto.setFirstName(FIRST_NAME);
        userResponseDto.setLastName(LAST_NAME);
        Set<String> roleDtos = new HashSet<>();
        roleDtos.add(WITHOUT_ROLE);
        userResponseDto.setRoleDtos(roleDtos);
        return userResponseDto;
    }

    private Role getDefaultRole() {
        final Role roleDefault = new Role();
        roleDefault.setId(ONE_ID);
        roleDefault.setName(Role.RoleName.WITHOUT_ROLE);
        return roleDefault;
    }

    private UserResponseDtoWithRole getUserResponseDtoWithRole() {
        final UserResponseDtoWithRole userResponseDto = new UserResponseDtoWithRole();
        userResponseDto.setId(TWO_ID);
        userResponseDto.setUsername(USERNAME);
        userResponseDto.setEmail(EMAIL);
        userResponseDto.setFirstName(FIRST_NAME);
        userResponseDto.setLastName(LAST_NAME);
        Set<String> roleDtos = new HashSet<>();
        roleDtos.add(ROLE_MANAGER);
        userResponseDto.setRoleDtos(roleDtos);
        return userResponseDto;
    }

    private User getAssignee() {
        final User user = new User();
        user.setId(TWO_ID);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setRoles(getRoles());
        return user;
    }

    private Set<Role> getRoles() {
        final Role roleUser = new Role();
        roleUser.setId(THREE_ID);
        roleUser.setName(Role.RoleName.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(getRoleManager());
        return roles;
    }

    private Role getRoleManager() {
        final Role roleManager = new Role();
        roleManager.setId(TWO_ID);
        roleManager.setName(Role.RoleName.ROLE_MANAGER);
        return roleManager;
    }
}
