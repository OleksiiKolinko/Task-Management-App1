package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.comment.CommentDto;
import mate.academy.dto.comment.CommentRequestDto;
import mate.academy.dto.comment.CommentResponseDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.mapper.CommentMapper;
import mate.academy.mapper.TaskMapper;
import mate.academy.model.Comment;
import mate.academy.model.Project;
import mate.academy.model.Role;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.repository.comment.CommentRepository;
import mate.academy.repository.role.RoleRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.repository.task.TaskSpecificationBuilder;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.impl.CommentServiceImpl;
import mate.academy.service.impl.PaginationUtilImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
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
    private static final int COMMENT_YEAR = 2145;
    private static final int ZERO = 0;
    private static final int DUE_YEAR = 2150;
    private static final String PASSWORD = "password";
    private static final int START_YEAR = 2100;
    private static final int END_YEAR = 2200;
    private static final PageRequest PAGEABLE = PageRequest.of(0, 50);
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final Long TWO_ID = 2L;
    private static final String TIMESTAMP_1 = "2145-01-01-00-00-00.000";
    private static final String TEXT_1 = "test1";
    private static final Role.RoleName ROLE_NAME_MANAGER = Role.RoleName.ROLE_MANAGER;
    private static final Role.RoleName ROLE_NAME_USER = Role.RoleName.ROLE_USER;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PaginationUtil paginationUtil;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskSpecificationBuilder taskSpecificationBuilder;
    @Mock
    private EmailMessageUtil emailMessageUtil;
    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("Verify save() method works")
    public void save_ValidComment_ReturnCommentResponseDto() {
        final CommentResponseDto expect = getCommentResponseDto();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getAssignee()));
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(getTask()));
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(getRoleManager()));
        when(commentRepository.save(any(Comment.class))).thenReturn(getComment());
        when(paginationUtil.paginateList(any(), anyList()))
                .thenReturn(new PaginationUtilImpl().paginateList(PAGEABLE, List.of(getComment())));
        when(commentMapper.toCommentDto(any(Comment.class)))
                .thenReturn(getCommentResponseDto().commentDtos().get(ZERO));
        when(taskMapper.toTaskResponseDto(any(Task.class))).thenReturn(getResponseTaskDto());
        final CommentResponseDto actual = commentService.save(ONE_ID,
                new CommentRequestDto(ONE_ID, TEXT_1));
        assertThat(actual).isEqualTo(expect);
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(taskRepository, times(ONE)).findById(anyLong());
        verify(roleRepository, times(ONE)).findById(anyLong());
        verify(commentRepository, times(ONE)).save(any(Comment.class));
        verify(commentMapper, times((ONE))).toCommentDto(any(Comment.class));
        verify(taskMapper, times(ONE)).toTaskResponseDto(any(Task.class));
        verify(userRepository, times(ONE)).findByRolesId(anyLong());
        verifyNoMoreInteractions(userRepository, taskRepository, roleRepository,
                commentRepository, taskMapper, commentMapper, paginationUtil);
    }

    @Test
    @DisplayName("Verify getAllComments() method works")
    public void getAllComments_ValidComment_returnPageCommentResponseDto() {
        final Page<CommentResponseDto> expect = getPageCommentResponseDto();
        final Task task = getTask();
        task.getComments().add(getComment());
        when(taskRepository.findAll(taskSpecificationBuilder
                .build(any(TaskSearchParameters.class)))).thenReturn(List.of(task));
        when(taskMapper.toTaskResponseDto(any(Task.class))).thenReturn(getResponseTaskDto());
        when(commentMapper.toCommentDto(any(Comment.class)))
                .thenReturn(getCommentResponseDto().commentDtos().get(ZERO));
        when(paginationUtil.paginateList(PAGEABLE, List.of(getCommentResponseDto())))
                .thenReturn(getPageCommentResponseDto());
        final Page<CommentResponseDto> actual = commentService
                .getAllComments(getTaskSearchParameters(), PAGEABLE);
        assertThat(actual).isEqualTo(expect);
        verify(taskRepository, times(ONE)).findAll(taskSpecificationBuilder
                .build(any(TaskSearchParameters.class)));
        verify(taskMapper, times(ONE)).toTaskResponseDto(any(Task.class));
        verify(commentMapper, times(ONE)).toCommentDto(any(Comment.class));
        verify(paginationUtil, times(ONE)).paginateList(PAGEABLE, List.of(getCommentResponseDto()));
        verifyNoMoreInteractions(commentRepository, taskMapper,
                commentMapper, paginationUtil, taskRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method works")
    public void deleteById_ValidComment_deleted() {
        final Comment comment = getComment();
        comment.setTask(getTask());
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        commentService.deleteById(ONE_ID, ONE_ID);
        verify(commentRepository, times(ONE)).deleteById(ONE_ID);
        verifyNoMoreInteractions(commentRepository);
    }

    private Page<CommentResponseDto> getPageCommentResponseDto() {
        return new PaginationUtilImpl().paginateList(PAGEABLE, List.of(getCommentResponseDto()));
    }

    private Comment getComment() {
        return Comment.builder().id(ONE_ID).user(getAssignee()).text(TEXT_1)
                .timestamp(LocalDateTime.of(COMMENT_YEAR, Month.JANUARY, ONE, ZERO, ZERO, ZERO))
                .build();
    }

    private CommentResponseDto getCommentResponseDto() {
        return new CommentResponseDto(getResponseTaskDto(),
                List.of(new CommentDto(ONE_ID, TIMESTAMP_1, USERNAME, TEXT_1)));
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
        return new ResponseTaskDto(ONE_ID, TASK_NAME,TASK_DESCRIPTION, MEDIUM, IN_PROGRESS,
                DUE_DATE, new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS), new UserResponseDtoWithRole(ONE_ID, USERNAME,
                EMAIL, FIRST_NAME, LAST_NAME, Set.of(ROLE_USER, ROLE_MANAGER)));
    }

    private Task getTask() {
        return Task.builder().id(ONE_ID).name(TASK_NAME).description(TASK_DESCRIPTION).priority(
                        Task.Priority.MEDIUM).status(Task.Status.IN_PROGRESS).dueDate(
                                LocalDate.of(DUE_YEAR,ONE,ONE)).project(Project.builder().id(ONE_ID)
                        .name(PROJECT_NAME).description(PROJECT_DESCRIPTION).startDate(
                                LocalDate.of(START_YEAR,ONE,ONE)).endDate(LocalDate.of(
                                        END_YEAR,ONE, ONE)).status(Project.Status.IN_PROGRESS)
                        .build()).assignee(getAssignee()).comments(new ArrayList<>()).build();
    }

    private User getAssignee() {
        return User.builder().id(ONE_ID).username(USERNAME).password(PASSWORD).email(EMAIL)
                .firstName(FIRST_NAME).lastName(LAST_NAME).roles(getRoles()).build();
    }

    private Set<Role> getRoles() {
        final Role roleUser = new Role();
        roleUser.setId(ONE_ID);
        roleUser.setName(ROLE_NAME_USER);
        return Set.of(roleUser, getRoleManager());
    }

    private Role getRoleManager() {
        final Role roleManager = new Role();
        roleManager.setId(TWO_ID);
        roleManager.setName(ROLE_NAME_MANAGER);
        return roleManager;
    }
}
