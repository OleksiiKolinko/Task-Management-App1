package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.label.CreateLabelDto;
import mate.academy.dto.label.ResponseLabelDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.mapper.LabelMapper;
import mate.academy.model.Label;
import mate.academy.model.Project;
import mate.academy.model.Role;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.repository.label.LabelRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.impl.LabelServiceImpl;
import mate.academy.service.impl.PaginationUtilImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class LabelServiceTest {
    private static final int ONE = 1;
    private static final Long ONE_ID = 1L;
    private static final String TASK_NAME = "taskName";
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
    private static final String COLOR = "color";
    private static final String LABEL_NANE = "labelName";
    private static final String UPDATE_LABEL_NAME = "updateLabelName";
    private static final int DUE_YEAR = 2150;
    private static final String PASSWORD = "password";
    private static final int START_YEAR = 2100;
    private static final int END_YEAR = 2200;
    private static final PageRequest PAGEABLE = PageRequest.of(0, 50);
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final Long TWO_ID = 2L;
    private static final Role.RoleName ROLE_NAME_MANAGER = Role.RoleName.ROLE_MANAGER;
    private static final Role.RoleName ROLE_NAME_USER = Role.RoleName.ROLE_USER;
    @Mock
    private LabelRepository labelRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private LabelMapper labelMapper;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private LabelServiceImpl labelService;

    @Test
    @DisplayName("Verify createLabel() method works")
    public void createLabel_Valid_ReturnResponseLabelDto() {
        ResponseLabelDto expect = getResponseLabelDto();
        when(labelRepository.findByName(LABEL_NANE)).thenReturn(Optional.empty());
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(getTask()));
        when(labelMapper.toResponseLabelDto(any(Label.class))).thenReturn(getResponseLabelDto());
        when(labelRepository.save(any(Label.class))).thenReturn(getLabel());
        ResponseLabelDto actual = labelService.createLabel(
                new CreateLabelDto(LABEL_NANE, COLOR, Set.of(ONE_ID)));
        assertThat(actual).isEqualTo(expect);
        verify(labelRepository, times(ONE)).findByName(LABEL_NANE);
        verify(taskRepository, times(ONE)).findById(anyLong());
        verify(labelMapper, times(ONE)).toResponseLabelDto(any(Label.class));
        verify(labelRepository, times(ONE)).save(any(Label.class));
        verify(labelRepository, times(ONE)).findByTasksId(anyLong());
        verifyNoMoreInteractions(labelRepository, taskRepository,
                labelMapper, labelRepository);
    }

    @Test
    @DisplayName("Verify findAll() method works")
    public void findAll_Valid_ReturnListResponseLabelDto() {
        List<ResponseLabelDto> expect = List.of(getResponseLabelDto());
        when(labelRepository.findAll(any(Pageable.class))).thenReturn(
                new PaginationUtilImpl().paginateList(PAGEABLE, List.of(getLabel())));
        when(labelMapper.toResponseLabelDto(any(Label.class))).thenReturn(getResponseLabelDto());
        List<ResponseLabelDto> actual = labelService.findAll(PAGEABLE);
        assertThat(actual).isEqualTo(expect);
        verify(labelRepository, times(ONE)).findAll(any(Pageable.class));
        verify(labelMapper, times(ONE)).toResponseLabelDto(any(Label.class));
        verifyNoMoreInteractions(labelRepository, labelMapper);
    }

    @Test
    @DisplayName("Verify updateById() method works")
    public void updateById_Valid_ReturnResponseLabelDto() {
        ResponseLabelDto expect = getUpdatedResponseLabelDto();
        when(labelRepository.findById(anyLong())).thenReturn(Optional.of(getLabel()));
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(getTask()));
        when(labelMapper.toResponseLabelDto(any(Label.class)))
                .thenReturn(getUpdatedResponseLabelDto());
        when(labelRepository.save(any(Label.class))).thenReturn(getUpdatedLabel());
        ResponseLabelDto actual = labelService.updateById(ONE_ID,
                new CreateLabelDto(UPDATE_LABEL_NAME, COLOR, Set.of(ONE_ID)));
        assertThat(actual).isEqualTo(expect);
        verify(labelRepository, times(ONE)).findByName(anyString());
        verify(taskRepository, times(ONE)).findById(anyLong());
        verify(labelMapper, times(ONE)).toResponseLabelDto(any(Label.class));
        verify(labelRepository, times(ONE)).save(any(Label.class));
        verify(labelRepository, times(ONE)).findByTasksId(anyLong());
        verifyNoMoreInteractions(labelRepository, taskRepository, labelMapper,
                labelRepository, userRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method works")
    public void deleteById_Valid_Deleted() {
        labelService.deleteById(ONE_ID);
        verify(labelRepository, times(ONE)).deleteById(ONE_ID);
        verifyNoMoreInteractions(labelRepository);
    }

    private ResponseLabelDto getUpdatedResponseLabelDto() {
        return new ResponseLabelDto(ONE_ID, UPDATE_LABEL_NAME, COLOR, Set.of(getResponseTaskDto()));
    }

    private Label getUpdatedLabel() {
        final Label label = getLabel();
        label.setName(UPDATE_LABEL_NAME);
        return label;
    }

    private Label getLabel() {
        return Label.builder().id(ONE_ID).name(LABEL_NANE).color(COLOR)
                .tasks(Set.of(getTask())).build();
    }

    private ResponseLabelDto getResponseLabelDto() {
        return new ResponseLabelDto(ONE_ID, LABEL_NANE, COLOR, Set.of(getResponseTaskDto()));
    }

    private ResponseTaskDto getResponseTaskDto() {
        return new ResponseTaskDto(ONE_ID, TASK_NAME,TASK_DESCRIPTION, MEDIUM, IN_PROGRESS,
                DUE_DATE, new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS), new UserResponseDtoWithRole(ONE_ID, USERNAME,
                EMAIL, FIRST_NAME, LAST_NAME, Set.of(ROLE_USER, ROLE_MANAGER)));
    }

    private Task getTask() {
        return Task.builder().id(ONE_ID).name(TASK_NAME).description(TASK_DESCRIPTION).priority(
                        Task.Priority.MEDIUM).status(Task.Status.IN_PROGRESS).dueDate(
                                LocalDate.of(DUE_YEAR,ONE,ONE)).project(Project.builder()
                .id(ONE_ID).name(PROJECT_NAME).description(PROJECT_DESCRIPTION).startDate(
                        LocalDate.of(START_YEAR,ONE,ONE)).endDate(LocalDate.of(END_YEAR,ONE, ONE))
                        .status(Project.Status.IN_PROGRESS).build())
                .assignee(getAssignee()).build();
    }

    private User getAssignee() {
        return User.builder().id(ONE_ID).username(USERNAME).password(PASSWORD).email(EMAIL)
                .firstName(FIRST_NAME).lastName(LAST_NAME).roles(getRoles()).build();
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
}
