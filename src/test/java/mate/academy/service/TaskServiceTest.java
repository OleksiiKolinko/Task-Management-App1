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
    private static final Role.RoleName ROLE_NAME_MANAGER = Role.RoleName.ROLE_MANAGER;
    private static final Role.RoleName ROLE_NAME_USER = Role.RoleName.ROLE_USER;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private EmailMessageUtil emailMessageUtil;
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
    private LabelService labelService;
    @Mock
    private LabelRepository labelRepository;
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
        taskService.deleteById(ONE_ID);
        verify(taskRepository, times(ONE)).findById(anyLong());
        verify(taskRepository, times(ONE)).deleteById(anyLong());
        verify(labelRepository, times(ONE)).deleteByTasksIsEmpty();
        verifyNoMoreInteractions(taskRepository, labelService, labelRepository);
    }

    private Task getUpdatedTask() {
        return Task.builder().id(ONE_ID).name(UPDATE_TASK_NAME).description(TASK_DESCRIPTION)
                .priority(Task.Priority.MEDIUM).status(Task.Status.IN_PROGRESS).dueDate(
                        LocalDate.of(DUE_YEAR,ONE,ONE)).project(getProject())
                .assignee(getAssignee()).build();
    }

    private ResponseTaskDto getUpdatedResponseTaskDto() {
        return new ResponseTaskDto(ONE_ID, UPDATE_TASK_NAME,TASK_DESCRIPTION, MEDIUM, IN_PROGRESS,
                DUE_DATE, getResponseProjectDto(), getUserResponseDtoWithRole());
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
        return new ResponseTaskDto(ONE_ID, TASK_NAME, TASK_DESCRIPTION, MEDIUM, IN_PROGRESS,
                DUE_DATE, getResponseProjectDto(), getUserResponseDtoWithRole());
    }

    private UserResponseDtoWithRole getUserResponseDtoWithRole() {
        return new UserResponseDtoWithRole(ONE_ID, USERNAME, EMAIL, FIRST_NAME, LAST_NAME,
                Set.of(ROLE_USER, ROLE_MANAGER));
    }

    private ResponseProjectDto getResponseProjectDto() {
        return new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS);
    }

    private Task getTask() {
        return Task.builder().id(ONE_ID).name(TASK_NAME).description(TASK_DESCRIPTION).priority(
                        Task.Priority.MEDIUM).status(Task.Status.IN_PROGRESS).dueDate(
                                LocalDate.of(DUE_YEAR,ONE,ONE)).project(getProject())
                .assignee(getAssignee()).attachments(new ArrayList<>()).build();
    }

    private User getAssignee() {
        return User.builder().id(ONE_ID).username(USERNAME).password(PASSWORD).email(EMAIL)
                .firstName(FIRST_NAME).lastName(LAST_NAME).roles(getRoles()).build();
    }

    private Set<Role> getRoles() {
        final Role roleUser = new Role();
        roleUser.setId(THREE_ID);
        roleUser.setName(ROLE_NAME_USER);
        final Role roleManager = new Role();
        roleManager.setId(TWO_ID);
        roleManager.setName(ROLE_NAME_MANAGER);
        return Set.of(roleUser, roleManager);
    }

    private Project getProject() {
        return Project.builder().id(ONE_ID).name(PROJECT_NAME).description(PROJECT_DESCRIPTION)
                .startDate(LocalDate.of(START_YEAR,ONE,ONE)).endDate(
                        LocalDate.of(END_YEAR,ONE, ONE)).status(Project.Status.IN_PROGRESS).build();
    }
}
