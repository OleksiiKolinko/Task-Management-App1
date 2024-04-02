package mate.academy.service.impl;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.user.RoleDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.exception.RegistrationException;
import mate.academy.mapper.UserMapper;
import mate.academy.model.Role;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.repository.role.RoleRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.EmailMessageUtil;
import mate.academy.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Long MANAGER_ROLE_ID = 2L;
    private static final String ROLE_USER = "ROLE_USER";
    private static final Long ID_ONE = 1L;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final TaskRepository taskRepository;
    private final EmailMessageUtil emailMessageUtil;

    @Override
    @Transactional
    public UserResponseDtoWithRole register(UserRegistrationRequestDto requestDto) {
        final String email = getValidEmail(requestDto.email());
        final String username = getValidUsername(requestDto.username());
        final Set<Role> defaultRole = Set.of(roleRepository.findById(ID_ONE).orElseThrow(
                () -> new EntityNotFoundException("The role with id: "
                        + ID_ONE + " is not exist")));
        final String firstName = requestDto.firstName();
        final String lastName = requestDto.lastName();
        final User newUser = User.builder().username(username).password(passwordEncoder.encode(
                requestDto.password())).email(email).firstName(firstName).lastName(lastName)
                .roles(defaultRole).build();
        final UserResponseDtoWithRole newUserDto = userMapper
                .toUserResponseWithRole(userRepository.save(newUser));
        emailMessageUtil.sendNewPerson(getUser(ID_ONE).getEmail(), newUserDto.getId(), username,
                email, firstName, lastName, newUserDto.getRoleDtos());
        return newUserDto;
    }

    @Override
    public UserResponseDtoWithRole updateRole(Long userId, RoleDto roleDto) {
        final User user = getUserUpdateRole(userId);
        final Set<Role> roles = getRoles(user, userId, roleDto);
        user.setRoles(roles);
        final UserResponseDtoWithRole userDtoWithRole = userMapper
                .toUserResponseWithRole(userRepository.save(user));
        emailMessageUtil.sendRoleUpdate(user.getEmail(), userDtoWithRole.getRoleDtos());
        return userDtoWithRole;
    }

    @Override
    public UserResponseDtoWithRole getProfileInfo(Long userId) {
        return userMapper.toUserResponseWithRole(getUser(userId));
    }

    @Override
    public UserResponseDtoWithRole updateProfileInfo(
            Long userId, UserRegistrationRequestDto requestDto) {
        final User userBefore = getUser(userId);
        userBefore.setUsername(updateValidUsername(requestDto.username(), userId));
        userBefore.setPassword(passwordEncoder.encode(requestDto.password()));
        userBefore.setEmail(updateValidEmail(requestDto.email(), userId));
        userBefore.setFirstName(requestDto.firstName());
        userBefore.setLastName(requestDto.lastName());
        return userMapper.toUserResponseWithRole(userRepository.save(userBefore));
    }

    private String updateValidUsername(String usernameRequestDto, Long userIdBefore) {
        userRepository.findByUsername(usernameRequestDto).ifPresent((foundUser) -> {
            if (!userIdBefore.equals(foundUser.getId())) {
                throw new RegistrationException("Someone registered with username "
                        + usernameRequestDto);
            }
        });
        return usernameRequestDto;
    }

    private String updateValidEmail(String emailRequestDto, Long userIdBefore) {
        userRepository.findByEmail(emailRequestDto).ifPresent((foundUser) -> {
            if (!userIdBefore.equals(foundUser.getId())) {
                throw new RegistrationException("The user with email "
                        + emailRequestDto + " is already registered");
            }
        });
        return emailRequestDto;
    }

    private String getValidEmail(String email) {
        userRepository.findByEmail(email).ifPresent((foundUser) -> {
            throw new RegistrationException("Someone registered with email "
                    + email);
        });
        return email;
    }

    private String getValidUsername(String username) {
        userRepository.findByUsername(username).ifPresent((foundUser) -> {
            throw new RegistrationException("Someone registered with username "
                    + username);
        });
        return username;
    }

    private Set<Role> getRoles(User user, Long userId, RoleDto roleDto) {
        final Set<Role> roles = roleDto.roles().stream()
                .map(this::getRoleByName)
                .collect(Collectors.toSet());
        final Role roleUser = getRoleByName(ROLE_USER);
        if (user.getRoles().contains(roleUser) && !roles.contains(roleUser)) {
            final Set<Task> tasksNotCompleted = taskRepository.findAllByAssigneeId(userId).stream()
                    .filter(task -> !task.getStatus().equals(Task.Status.COMPLETED))
                    .collect(Collectors.toSet());
            if (!tasksNotCompleted.isEmpty()) {
                emailMessageUtil.sendChangeAssignee(userRepository.findByRolesId(MANAGER_ROLE_ID)
                        .stream()
                        .map(User::getEmail).collect(Collectors.toSet()), tasksNotCompleted, user);
            }
        }
        return roles;
    }

    private Role getRoleByName(String roleName) {
        return roleRepository.findByName(Role.RoleName.valueOf(roleName))
                .orElseThrow(() -> new EntityNotFoundException("Can't find role by name "
                        + roleName));
    }

    private User getUserUpdateRole(Long userId) {
        if (ID_ONE.equals(userId)) {
            throw new EntityNotFoundException("The update roles for admin is forbidden");
        }
        return getUser(userId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + userId + " is not exist"));
    }
}
