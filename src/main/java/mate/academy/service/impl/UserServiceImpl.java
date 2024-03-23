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
import mate.academy.service.EmailService;
import mate.academy.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Long MANAGER_ROLE_ID = 2L;
    private static final String ROLE_USER = "ROLE_USER";
    private static final String NEW_PERSON = "New person";
    private static final String UPDATE_ROLES = "Your roles updated";
    private static final String TOP_BASE_NEW_PERSON = "The registered new person, please, "
            + "put him roles. Information about him:";
    private static final String USER_ID = "User identification   ";
    private static final String USERNAME = "Username                  ";
    private static final String EMAIL = "Email                           ";
    private static final String FIRST_NAME = "First name                  ";
    private static final String LAST_NAME = "Last name                   ";
    private static final String ROLES = "Roles                           ";
    private static final String TOP_BASE_UPDATE_ROLES = "The admin updated your roles. "
            + "Your new roles: ";
    private static final String PUT_ASSIGNEE = "Put new assignee for the task";
    private static final String TASK_ID = "-- task id ";
    private static final String TASK_NAME = ", with task name ";
    private static final String FROM_PROJECT_ID = ", from project with id ";
    private static final String PROJECT_NAME = " and project name ";
    private static final String ROLE_USER_OFF =
            "The admin had taken ROLE_USER off from user with id ";
    private static final String WITH_USERNAME = " and with username ";
    private static final String CHANGE_ASSIGNEE = ". So need change assignee for tasks:";
    private static final int ZERO = 0;
    private static final Long ID_ONE = 1L;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public UserResponseDtoWithRole register(UserRegistrationRequestDto requestDto) {
        final String email = requestDto.email();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RegistrationException("This email address is already registered");
        }
        final String username = requestDto.username();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RegistrationException("Someone registered with this username");
        }
        final Set<Role> defaultRole = Set.of(roleRepository.findById(ID_ONE).orElseThrow(
                () -> new EntityNotFoundException("The role with id: "
                        + ID_ONE + " is not exist")));
        final String firstName = requestDto.firstName();
        final String lastName = requestDto.lastName();
        final User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(requestDto.password()));
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setRoles(defaultRole);
        final UserResponseDtoWithRole newUserDto = userMapper
                .toUserResponseWithRole(userRepository.save(newUser));
        newUserDto.setRoleDtos(getRoleDtos(defaultRole));
        emailService.sendEmail(userRepository.findById(ID_ONE).orElseThrow(
                        () -> new EntityNotFoundException("Cant find user by id: " + ID_ONE))
                .getEmail(), NEW_PERSON, getBodyTextNewPerson(newUserDto.getId(),
                username, email, firstName, lastName, newUserDto.getRoleDtos()));
        return newUserDto;
    }

    @Override
    @Transactional
    public UserResponseDtoWithRole updateRole(Long userId, RoleDto roleDto) {
        if (userId.equals(ID_ONE)) {
            throw new EntityNotFoundException("The update roles for admin is forbidden");
        }
        Set<Role> roles = roleDto.roles().stream()
                .map(r -> {
                    try {
                        return roleRepository.findByName(Role.RoleName.valueOf(r)).orElseThrow(
                                RuntimeException::new);
                    } catch (RuntimeException e) {
                        throw new EntityNotFoundException("The role with name " + r
                                + " is not exist. There are three roles: "
                                + "ROLE_ADMIN, ROLE_MANAGER, ROLE_USER and WITHOUT_ROLE");
                    }
                })
                .collect(Collectors.toSet());
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + userId));
        final Role roleUser = roleRepository.findByName(Role.RoleName.valueOf(ROLE_USER))
                .orElseThrow(() -> new EntityNotFoundException("Can't find role"));
        if (user.getRoles().contains(roleUser) && !roles.contains(roleUser)) {
            final Set<Task> tasks = taskRepository.findAllByAssigneeId(userId);
            if (!tasks.isEmpty()) {
                userRepository.findByRolesId(MANAGER_ROLE_ID)
                        .forEach(m -> emailService.sendEmail(m.getEmail(),
                                PUT_ASSIGNEE, getBodyTextNewAssignee(tasks, user)));
            }
        }
        user.setRoles(roles);
        final UserResponseDtoWithRole userDtoWithRole = userMapper
                .toUserResponseWithRole(userRepository.save(user));
        Set<String> roleDtos = getRoleDtos(roles);
        userDtoWithRole.setRoleDtos(roleDtos);
        emailService.sendEmail(user.getEmail(), UPDATE_ROLES, TOP_BASE_UPDATE_ROLES + roleDtos);
        return userDtoWithRole;
    }

    @Override
    public UserResponseDtoWithRole getProfileInfo(Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + userId + " is not exist"));
        final UserResponseDtoWithRole userDtoWithRole = userMapper.toUserResponseWithRole(user);
        userDtoWithRole.setRoleDtos(getRoleDtos(user.getRoles()));
        return userDtoWithRole;
    }

    @Transactional
    @Override
    public UserResponseDtoWithRole updateProfileInfo(
            Long userId, UserRegistrationRequestDto requestDto) {
        final String emailRequestDto = requestDto.email();
        final User userBefore = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + userId + " is not exist"));
        if (userRepository.findByEmail(emailRequestDto).isPresent()
                && !userBefore.getEmail().equals(emailRequestDto)) {
            throw new RegistrationException("This email address is already registered");
        }
        final String usernameRequestDto = requestDto.username();
        if (userRepository.findByUsername(usernameRequestDto).isPresent()
                && !userBefore.getUsername().equals(usernameRequestDto)) {
            throw new RegistrationException("Someone registered with this username");
        }
        userBefore.setUsername(usernameRequestDto);
        userBefore.setPassword(passwordEncoder.encode(requestDto.password()));
        userBefore.setEmail(emailRequestDto);
        userBefore.setFirstName(requestDto.firstName());
        userBefore.setLastName(requestDto.lastName());
        final UserResponseDtoWithRole userDtoWithRole = userMapper
                .toUserResponseWithRole(userRepository.save(userBefore));
        userDtoWithRole.setRoleDtos(getRoleDtos(userBefore.getRoles()));
        return userDtoWithRole;
    }

    private Set<String> getRoleDtos(Set<Role> userRoles) {
        return userRoles.stream().map(r -> r.getName().toString()).collect(Collectors.toSet());
    }

    private String getBodyTextNewPerson(Long userId, String username, String email,
                                        String firstName, String lastName,
                                        Set<String> defaultRole) {
        return new StringBuilder(TOP_BASE_NEW_PERSON).append(System.lineSeparator()).append(USER_ID)
                .append(userId).append(System.lineSeparator()).append(USERNAME).append(username)
                .append(System.lineSeparator()).append(EMAIL).append(email)
                .append(System.lineSeparator()).append(FIRST_NAME).append(firstName)
                .append(System.lineSeparator()).append(LAST_NAME).append(lastName)
                .append(System.lineSeparator()).append(ROLES).append(defaultRole).toString();
    }

    private String getBodyTextNewAssignee(Set<Task> tasks, User user) {
        final StringBuilder sum = new StringBuilder();
        final String tasksText = tasks.stream()
                .map(t -> {
                    sum.setLength(ZERO);
                    return sum.append(System.lineSeparator()).append(TASK_ID).append(t.getId())
                            .append(TASK_NAME).append(t.getName()).append(FROM_PROJECT_ID)
                            .append(t.getProject().getId()).append(PROJECT_NAME)
                            .append(t.getProject().getName()); })
                .collect(Collectors.joining());
        return new StringBuilder(ROLE_USER_OFF).append(user.getId()).append(WITH_USERNAME)
                .append(user.getUsername()).append(CHANGE_ASSIGNEE).append(tasksText).toString();
    }
}
