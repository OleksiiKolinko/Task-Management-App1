package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskDtoCreate;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.mapper.TaskMapper;
import mate.academy.model.Project;
import mate.academy.model.Role;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.repository.attachment.AttachmentRepository;
import mate.academy.repository.comment.CommentRepository;
import mate.academy.repository.label.LabelRepository;
import mate.academy.repository.project.ProjectRepository;
import mate.academy.repository.role.RoleRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.repository.task.TaskSpecificationBuilder;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.impl.PaginationUtilImpl;
import mate.academy.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    private static final int ONE = 1;
    private static final Long ONE_ID = 1L;
    private static final String TASK_NAME = "taskName";
    private static final String UPDATE_TASK_NAME = "updateTaskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final String MEDIUM = "MEDIUM";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String DUE_DATE = "2150-01-01";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email@example.com";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String START_DATE = "2100-01-01";
    private static final String END_DATE = "2200-01-01";
    private static final int ZERO = 0;
    private static final int DUE_YEAR = 2150;
    private static final String PASSWORD = "password";
    private static final int START_YEAR = 2100;
    private static final int END_YEAR = 2200;
    private static final PageRequest PAGEABLE = PageRequest.of(0, 20);
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final Long TWO_ID = 2L;
    private static final Long THREE_ID = 3L;
    private static final Long FOUR_ID = 4L;
    private static final int TWO = 2;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskSpecificationBuilder taskSpecificationBuilder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private LabelRepository labelRepository;
    @Mock
    private AttachmentRepository attachmentRepository;
    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    @DisplayName("Verify createTask() method works")
    public void createTask_Valid_ResponseTaskDto() {
        final ResponseTaskDto expect = getResponseTaskDto();
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(getProject()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getAssignee()));
        when(roleRepository.findById(THREE_ID)).thenReturn(getRoles().stream().findFirst());
        when(roleRepository.findById(FOUR_ID)).thenReturn(Optional.of(getRoleAdmin()));
        when(taskRepository.save(any(Task.class))).thenReturn(getTask());
        when(taskMapper.toTaskResponseDto(any(Task.class))).thenReturn(getResponseTaskDto());
        final ResponseTaskDto actual = taskService.createTask(
                new TaskDtoCreate(TASK_NAME, TASK_DESCRIPTION, MEDIUM,
                        IN_PROGRESS, DUE_DATE, ONE_ID, ONE_ID));
        assertThat(actual).isEqualTo(expect);
        verify(projectRepository, times(ONE)).findById(anyLong());
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(roleRepository, times(TWO)).findById(anyLong());
        verify(taskRepository, times(ONE)).save(any(Task.class));
        verify(taskMapper, times(ONE)).toTaskResponseDto(any(Task.class));
        verify(taskRepository, times(ONE)).findByName(anyString());
        verifyNoMoreInteractions(projectRepository, userRepository,
                roleRepository, taskRepository, taskMapper);
    }

    @Test
    @DisplayName("Verify getAllTasks() method works")
    public void getAllTasks_Valid_ReturnListResponseTaskDto() {
        final List<ResponseTaskDto> expect = List.of(getResponseTaskDto());
        when(taskRepository.findAll(taskSpecificationBuilder
                .build(any(TaskSearchParameters.class)), PAGEABLE))
                .thenReturn(new PaginationUtilImpl().paginateList(PAGEABLE, List.of(getTask())));
        when(taskMapper.toTaskResponseDto(any(Task.class))).thenReturn(getResponseTaskDto());
        final List<ResponseTaskDto> actual = taskService
                .getAllTasks(getTaskSearchParameters(), PAGEABLE);
        assertThat(actual).isEqualTo(expect);
        verify(taskRepository, times(ONE)).findAll(taskSpecificationBuilder
                .build(any(TaskSearchParameters.class)), PAGEABLE);
        verify(taskMapper, times(ONE)).toTaskResponseDto(any(Task.class));
        verifyNoMoreInteractions(taskRepository, taskMapper);
    }

    @Test
    @DisplayName("Verify getTaskById() method works")
    public void getTaskById_Valid_ReturnResponseTaskDto() {
        final ResponseTaskDto expect = getResponseTaskDto();
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(getTask()));
        when(taskMapper.toTaskResponseDto(any(Task.class))).thenReturn(getResponseTaskDto());
        final ResponseTaskDto actual = taskService.getTaskById(ONE_ID);
        assertThat(actual).isEqualTo(expect);
        verify(taskRepository, times(ONE)).findById(anyLong());
        verify(taskMapper, times(ONE)).toTaskResponseDto(any(Task.class));
        verifyNoMoreInteractions(taskRepository, taskMapper);
    }

    @Test
    @DisplayName("Verify updateTaskById() method works")
    public void updateTaskById_Valid_ReturnResponseTaskDto() {
        final ResponseTaskDto expect = getUpdatedResponseTaskDto();
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(getTask()));
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(getProject()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getAssignee()));
        when(roleRepository.findById(THREE_ID)).thenReturn(getRoles().stream().findFirst());
        when(roleRepository.findById(FOUR_ID)).thenReturn(Optional.of(getRoleAdmin()));
        when(taskRepository.save(any(Task.class))).thenReturn(getUpdatedTask());
        when(taskMapper.toTaskResponseDto(any(Task.class))).thenReturn(getUpdatedResponseTaskDto());
        final ResponseTaskDto actual = taskService.updateTaskById(ONE_ID,
                new TaskDtoCreate(UPDATE_TASK_NAME, TASK_DESCRIPTION, MEDIUM, IN_PROGRESS,
                        DUE_DATE, ONE_ID, ONE_ID));
        assertThat(actual).isEqualTo(expect);
        verify(taskRepository, times(ONE)).findById(anyLong());
        verify(projectRepository, times(ONE)).findById(anyLong());
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(roleRepository, times(TWO)).findById(anyLong());
        verify(taskRepository, times(ONE)).save(any(Task.class));
        verify(taskMapper, times(ONE)).toTaskResponseDto(any(Task.class));
        verify(taskRepository, times(ONE)).findByName(anyString());
        verifyNoMoreInteractions(projectRepository, userRepository, roleRepository,
                taskRepository, taskMapper);
    }

    @Test
    @DisplayName("Verify deleteById() method works")
    public void deleteById_Valid_Deleted() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(getTask()));
        when(commentRepository.findByTaskId(anyLong())).thenReturn(new ArrayList<>());
        when(labelRepository.findByTasksId(anyLong())).thenReturn(Optional.empty());
        taskService.deleteById(ONE_ID);
        verify(taskRepository, times(ONE)).findById(anyLong());
        verify(commentRepository, times(ONE)).findByTaskId(anyLong());
        verify(labelRepository, times(ONE)).findByTasksId(anyLong());
        verify(taskRepository, times(ONE)).deleteById(anyLong());
        verify(attachmentRepository, times(ONE)).findAllByTaskId(anyLong());
        verifyNoMoreInteractions(taskRepository, commentRepository,
                labelRepository, attachmentRepository);
    }

    private Task getUpdatedTask() {
        final Task task = new Task();
        task.setId(ONE_ID);
        task.setName(UPDATE_TASK_NAME);
        task.setDescription(TASK_DESCRIPTION);
        task.setPriority(Task.Priority.MEDIUM);
        task.setStatus(Task.Status.IN_PROGRESS);
        task.setDueDate(LocalDate.of(DUE_YEAR,ONE,ONE));
        task.setProject(getProject());
        task.setAssignee(getAssignee());
        return task;
    }

    private ResponseTaskDto getUpdatedResponseTaskDto() {
        final ResponseTaskDto responseTaskDto = new ResponseTaskDto();
        responseTaskDto.setId(ONE_ID);
        responseTaskDto.setName(UPDATE_TASK_NAME);
        responseTaskDto.setDescription(TASK_DESCRIPTION);
        responseTaskDto.setPriority(MEDIUM);
        responseTaskDto.setStatus(IN_PROGRESS);
        responseTaskDto.setDueDate(DUE_DATE);
        responseTaskDto.setProject(getResponseProjectDto());
        responseTaskDto.setAssignee(getUserResponseDtoWithRole());
        return responseTaskDto;
    }

    private Role getRoleAdmin() {
        final Role roleAdmin = new Role();
        roleAdmin.setId(FOUR_ID);
        roleAdmin.setName(Role.RoleName.ROLE_ADMIN);
        return roleAdmin;
    }

    private TaskSearchParameters getTaskSearchParameters() {
        final Long[] taskIds = new Long[ONE];
        taskIds[ZERO] = ONE_ID;
        final String[] taskName = new String[ONE];
        taskName[ZERO] = TASK_NAME;
        final Long[] projectIds = new Long[ONE];
        projectIds[ZERO] = ONE_ID;
        final String[] projectName = new String[ONE];
        projectName[ZERO] = PROJECT_NAME;
        final Long[] assigneeIds = new Long[ONE];
        assigneeIds[ZERO] = ONE_ID;
        final String[] assigneeNames = new String[ONE];
        assigneeNames[ZERO] = USERNAME;
        return new TaskSearchParameters(taskIds, taskName, projectIds,
                projectName, assigneeIds, assigneeNames);
    }

    private ResponseTaskDto getResponseTaskDto() {
        final ResponseTaskDto responseTaskDto = new ResponseTaskDto();
        responseTaskDto.setId(ONE_ID);
        responseTaskDto.setName(TASK_NAME);
        responseTaskDto.setDescription(TASK_DESCRIPTION);
        responseTaskDto.setPriority(MEDIUM);
        responseTaskDto.setStatus(IN_PROGRESS);
        responseTaskDto.setDueDate(DUE_DATE);
        responseTaskDto.setProject(getResponseProjectDto());
        responseTaskDto.setAssignee(getUserResponseDtoWithRole());
        return responseTaskDto;
    }

    private UserResponseDtoWithRole getUserResponseDtoWithRole() {
        final UserResponseDtoWithRole userResponseDto = new UserResponseDtoWithRole();
        userResponseDto.setId(ONE_ID);
        userResponseDto.setUsername(USERNAME);
        userResponseDto.setEmail(EMAIL);
        userResponseDto.setFirstName(FIRST_NAME);
        userResponseDto.setLastName(LAST_NAME);
        Set<String> roleDtos = new HashSet<>();
        roleDtos.add(ROLE_USER);
        roleDtos.add(ROLE_MANAGER);
        userResponseDto.setRoleDtos(roleDtos);
        return userResponseDto;
    }

    private ResponseProjectDto getResponseProjectDto() {
        return new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS);
    }

    private Task getTask() {
        final Task task = new Task();
        task.setId(ONE_ID);
        task.setName(TASK_NAME);
        task.setDescription(TASK_DESCRIPTION);
        task.setPriority(Task.Priority.MEDIUM);
        task.setStatus(Task.Status.IN_PROGRESS);
        task.setDueDate(LocalDate.of(DUE_YEAR,ONE,ONE));
        task.setProject(getProject());
        task.setAssignee(getAssignee());
        return task;
    }

    private User getAssignee() {
        final User user = new User();
        user.setId(ONE_ID);
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
        roles.add(roleUser);
        roles.add(getRoleManager());
        return roles;
    }

    private Role getRoleManager() {
        final Role roleManager = new Role();
        roleManager.setId(TWO_ID);
        roleManager.setName(Role.RoleName.ROLE_MANAGER);
        return roleManager;
    }

    private Project getProject() {
        final Project project = new Project();
        project.setId(ONE_ID);
        project.setName(PROJECT_NAME);
        project.setDescription(PROJECT_DESCRIPTION);
        project.setStartDate(LocalDate.of(START_YEAR,ONE,ONE));
        project.setEndDate(LocalDate.of(END_YEAR,ONE, ONE));
        project.setStatus(Project.Status.IN_PROGRESS);
        return project;
    }
}
