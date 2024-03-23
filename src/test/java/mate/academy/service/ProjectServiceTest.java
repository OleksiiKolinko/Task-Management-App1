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
import java.util.ArrayList;
import java.util.HashSet;
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
import mate.academy.repository.attachment.AttachmentRepository;
import mate.academy.repository.comment.CommentRepository;
import mate.academy.repository.label.LabelRepository;
import mate.academy.repository.project.ProjectRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.service.impl.PaginationUtilImpl;
import mate.academy.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private LabelRepository labelRepository;
    @Mock
    private EmailService emailService;
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
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(getPageProjects());
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
        when(projectRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(getProject()));
        when(projectMapper.toResponseProjectDto(any(Project.class)))
                .thenReturn(getResponseProjectDto());
        final ResponseProjectDto actual = projectService.getProject(ONE_ID);
        assertThat(actual).isEqualTo(expect);
        verify(projectRepository, times(ONE)).findById(Mockito.anyLong());
        verify(projectMapper, times(ONE)).toResponseProjectDto(any(Project.class));
        verifyNoMoreInteractions(projectRepository, projectMapper);
    }

    @Test
    @DisplayName("Verify updateProject() method works")
    public void updateProject_Valid_ReturnResponseProjectDto() {
        final ResponseProjectDto expect = getUpdatedResponseProjectDto();
        when(projectRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(getProject()));
        when(taskRepository.findAllByProjectId(Mockito.anyLong())).thenReturn(Set.of(getTask()));
        when(projectMapper.toResponseProjectDto(any(Project.class)))
                .thenReturn(getUpdatedResponseProjectDto());
        when(projectRepository.save(any(Project.class))).thenReturn(getUpdatedProject());
        final ResponseProjectDto actual = projectService.updateProject(ONE_ID,
                new UpdateProjectDto(UPDATE_PROJECT_NAME, PROJECT_DESCRIPTION,
                        START_DATE, END_DATE, IN_PROGRESS));
        assertThat(actual).isEqualTo(expect);
        verify(projectRepository, times(ONE)).findById(Mockito.anyLong());
        verify(taskRepository, times(ONE)).findAllByProjectId(Mockito.anyLong());
        verify(projectMapper, times(ONE)).toResponseProjectDto(any(Project.class));
        verify(projectRepository, times(ONE)).save(any(Project.class));
        verify(projectRepository, times(ONE)).findByName(anyString());
        verifyNoMoreInteractions(projectRepository, projectMapper, taskRepository);
    }

    @Test
    @DisplayName("Verify deleteProject() method works")
    public void deleteProject_Valid_Deleted() {
        when(taskRepository.findAllByProjectId(Mockito.anyLong())).thenReturn(Set.of(getTask()));
        when(commentRepository.findByTaskId(Mockito.anyLong())).thenReturn(new ArrayList<>());
        when(labelRepository.findByTasksId(Mockito.anyLong())).thenReturn(Optional.empty());
        projectService.deleteProject(ONE_ID);
        verify(taskRepository, times(ONE)).findAllByProjectId(Mockito.anyLong());
        verify(commentRepository, times(ONE)).findByTaskId(Mockito.anyLong());
        verify(attachmentRepository, times(ONE)).findAllByTaskId(Mockito.anyLong());
        verify(taskRepository, times(ONE)).deleteById(anyLong());
        verifyNoMoreInteractions(taskRepository, commentRepository, attachmentRepository);
    }

    private Project getUpdatedProject() {
        final Project project = new Project();
        project.setId(ONE_ID);
        project.setName(UPDATE_PROJECT_NAME);
        project.setDescription(PROJECT_DESCRIPTION);
        project.setStartDate(LocalDate.of(START_YEAR,ONE,ONE));
        project.setEndDate(LocalDate.of(END_YEAR,ONE, ONE));
        project.setStatus(Project.Status.IN_PROGRESS);
        return project;
    }

    private ResponseProjectDto getUpdatedResponseProjectDto() {
        return new ResponseProjectDto(ONE_ID, UPDATE_PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS);
    }

    private Page<Project> getPageProjects() {
        final PaginationUtil paginationUtil = new PaginationUtilImpl();
        final List<Project> projects = new ArrayList<>();
        projects.add(getProject());
        return paginationUtil.paginateList(PAGEABLE, projects);
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
        roleUser.setId(ONE_ID);
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
