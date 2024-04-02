package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.project.CreateProjectDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.project.UpdateProjectDto;
import mate.academy.mapper.ProjectMapper;
import mate.academy.model.Project;
import mate.academy.model.Role;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.repository.project.ProjectRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.service.impl.PaginationUtilImpl;
import mate.academy.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    private static final int ONE = 1;
    private static final Long ONE_ID = 1L;
    private static final String TASK_NAME = "taskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email@example.com";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PROJECT_NAME = "projectName";
    private static final String UPDATE_PROJECT_NAME = "updateProjectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String START_DATE = "2100-01-01";
    private static final String END_DATE = "2200-01-01";
    private static final int DUE_YEAR = 2150;
    private static final String PASSWORD = "password";
    private static final int START_YEAR = 2100;
    private static final int END_YEAR = 2200;
    private static final PageRequest PAGEABLE = PageRequest.of(0, 20);
    private static final Long TWO_ID = 2L;
    private static final Role.RoleName ROLE_NAME_MANAGER = Role.RoleName.ROLE_MANAGER;
    private static final Role.RoleName ROLE_NAME_USER = Role.RoleName.ROLE_USER;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private LabelService labelService;
    @Mock
    private TaskService taskService;
    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("Verify createProject() method works")
    public void createProject_Valid_ReturnResponseProjectDto() {
        final ResponseProjectDto expect = getResponseProjectDto();
        when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.empty());
        when(projectMapper.toResponseProjectDto(any(Project.class)))
                .thenReturn(getResponseProjectDto());
        when(projectRepository.save(any(Project.class))).thenReturn(getProject());
        final ResponseProjectDto actual = projectService.createProject(
                new CreateProjectDto(PROJECT_NAME, PROJECT_DESCRIPTION, START_DATE, END_DATE));
        assertThat(actual).isEqualTo(expect);
        verify(projectRepository, times(ONE)).findByName(PROJECT_NAME);
        verify(projectMapper, times(ONE)).toResponseProjectDto(any(Project.class));
        verify(projectRepository, times(ONE)).save(any(Project.class));
        verifyNoMoreInteractions(projectRepository, projectMapper, projectRepository);
    }

    @Test
    @DisplayName("Verify getAllProjects() method works")
    public void getAllProjects_Valid_ReturnListResponseProjectDto() {
        final List<ResponseProjectDto> expect = List.of(getResponseProjectDto());
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(new PaginationUtilImpl()
                .paginateList(PAGEABLE, List.of(getProject())));
        when(projectMapper.toResponseProjectDto(any(Project.class)))
                .thenReturn(getResponseProjectDto());
        final List<ResponseProjectDto> actual = projectService.getAllProjects(PAGEABLE);
        assertThat(actual).isEqualTo(expect);
        verify(projectRepository, times(ONE)).findAll(any(Pageable.class));
        verify(projectMapper, times(ONE)).toResponseProjectDto(any(Project.class));
        verifyNoMoreInteractions(projectRepository, projectMapper);
    }

    @Test
    @DisplayName("Verify getProject() method works")
    public void getProject_Valid_ReturnResponseProjectDto() {
        final ResponseProjectDto expect = getResponseProjectDto();
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(getProject()));
        when(projectMapper.toResponseProjectDto(any(Project.class)))
                .thenReturn(getResponseProjectDto());
        final ResponseProjectDto actual = projectService.getProject(ONE_ID);
        assertThat(actual).isEqualTo(expect);
        verify(projectRepository, times(ONE)).findById(anyLong());
        verify(projectMapper, times(ONE)).toResponseProjectDto(any(Project.class));
        verifyNoMoreInteractions(projectRepository, projectMapper);
    }

    @Test
    @DisplayName("Verify updateProject() method works")
    public void updateProject_Valid_ReturnResponseProjectDto() {
        final ResponseProjectDto expect = getUpdatedResponseProjectDto();
        final Project project = getProject();
        project.setTasks(Set.of(getTask()));
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(projectMapper.toResponseProjectDto(any(Project.class)))
                .thenReturn(getUpdatedResponseProjectDto());
        when(projectRepository.save(any(Project.class))).thenReturn(getUpdatedProject());
        final ResponseProjectDto actual = projectService.updateProject(ONE_ID,
                new UpdateProjectDto(UPDATE_PROJECT_NAME, PROJECT_DESCRIPTION,
                        START_DATE, END_DATE, IN_PROGRESS));
        assertThat(actual).isEqualTo(expect);
        verify(projectRepository, times(ONE)).findById(anyLong());
        verify(projectMapper, times(ONE)).toResponseProjectDto(any(Project.class));
        verify(projectRepository, times(ONE)).save(any(Project.class));
        verify(projectRepository, times(ONE)).findByName(anyString());
        verifyNoMoreInteractions(projectRepository, projectMapper, taskRepository);
    }

    @Test
    @DisplayName("Verify deleteProject() method works")
    public void deleteProject_Valid_Deleted() {
        final Project project = getProject();
        project.setTasks(Set.of(getTask()));
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        projectService.deleteProject(ONE_ID);
        verify(taskService, times(ONE)).deleteFilesLabelIfTasksIsEmptyAndSendEmail(any(Task.class));
        verify(projectRepository, times(ONE)).deleteById(anyLong());
        verifyNoMoreInteractions(taskRepository, labelService, projectRepository, taskService);
    }

    private Project getUpdatedProject() {
        return Project.builder().id(ONE_ID).name(UPDATE_PROJECT_NAME).description(
                PROJECT_DESCRIPTION).startDate(LocalDate.of(START_YEAR,ONE,ONE))
                .endDate(LocalDate.of(END_YEAR,ONE, ONE))
                .status(Project.Status.IN_PROGRESS).tasks(Set.of(getTask())).build();
    }

    private ResponseProjectDto getUpdatedResponseProjectDto() {
        return new ResponseProjectDto(ONE_ID, UPDATE_PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS);
    }

    private ResponseProjectDto getResponseProjectDto() {
        return new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS);
    }

    private Task getTask() {
        return Task.builder().id(ONE_ID).name(TASK_NAME).description(TASK_DESCRIPTION).priority(
                        Task.Priority.MEDIUM).status(Task.Status.IN_PROGRESS).dueDate(
                                LocalDate.of(DUE_YEAR,ONE,ONE)).project(getProject())
                .assignee(User.builder().id(ONE_ID).username(USERNAME).password(PASSWORD)
                        .email(EMAIL).firstName(FIRST_NAME).lastName(LAST_NAME)
                        .roles(getRoles()).build()).build();
    }

    private Set<Role> getRoles() {
        final Role roleUser = new Role();
        roleUser.setId(ONE_ID);
        roleUser.setName(ROLE_NAME_USER);
        final Role roleManager = new Role();
        roleManager.setId(TWO_ID);
        roleManager.setName(ROLE_NAME_MANAGER);
        return Set.of(roleUser, roleManager);
    }

    private Project getProject() {
        return Project.builder().id(ONE_ID).name(PROJECT_NAME).description(PROJECT_DESCRIPTION)
                .startDate(LocalDate.of(START_YEAR,ONE,ONE)).endDate(LocalDate.of(
                        END_YEAR,ONE, ONE)).status(Project.Status.IN_PROGRESS).build();
    }
}
