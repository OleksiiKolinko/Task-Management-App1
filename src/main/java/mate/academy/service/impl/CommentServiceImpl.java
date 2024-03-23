package mate.academy.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.comment.CommentDto;
import mate.academy.dto.comment.CommentRequestDto;
import mate.academy.dto.comment.CommentResponseDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.CommentMapper;
import mate.academy.mapper.TaskMapper;
import mate.academy.model.Comment;
import mate.academy.model.Role;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.repository.comment.CommentRepository;
import mate.academy.repository.role.RoleRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.repository.task.TaskSpecificationBuilder;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.CommentService;
import mate.academy.service.EmailService;
import mate.academy.service.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private static final Long ADMIN_ID = 1L;
    private static final Long MANAGER_ROLE_ID = 2L;
    private static final String NEW_COMMENT = "New comment";
    private static final String COMMENT_REMOVED = "Comment removed";
    private static final String TOP_BODY_TEXT_NEW_COMMENT =
            "This comment was written" + System.lineSeparator();
    private static final int COMMENTS_LIMIT = 50;
    private static final int ZERO = 0;
    private static final String BODY_TEXT_COMMENT_REMOVED =
            "This comment removed" + System.lineSeparator();
    private static final String COMMENT_ID = "The comment with id ";
    private static final String WAS_WRITTEN = " was written by ";
    private static final String WITH_USER_ID = " with user id ";
    private static final String FOR_TASK = " for task ";
    private static final String WITH_TASK_ID = " with task id ";
    private static final String AT = " at ";
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final RoleRepository roleRepository;
    private final CommentRepository commentRepository;
    private final TaskMapper taskMapper;
    private final CommentMapper commentMapper;
    private final EmailService emailService;
    private final PaginationUtil paginationUtil;
    private final TaskSpecificationBuilder taskSpecificationBuilder;

    @Transactional
    @Override
    public CommentResponseDto save(Long userId, CommentRequestDto requestDto) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + userId));
        final Long taskId = requestDto.taskId();
        final Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("Cant find task by id: " + taskId));
        final Role manegerRole = roleRepository.findById(MANAGER_ROLE_ID).orElseThrow(
                () -> new EntityNotFoundException("Cant find role by id: " + MANAGER_ROLE_ID));
        final User assignee = task.getAssignee();
        if (!user.getRoles().contains(manegerRole) && !user.equals(assignee)) {
            throw new EntityNotFoundException(
                    "You can comment only yours tasks, or if you have ROLE_MANAGER");
        }
        final Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setText(requestDto.text());
        comment.setTimestamp(LocalDateTime.now());
        commentRepository.save(comment);
        final Pageable pageable = PageRequest.of(ZERO, COMMENTS_LIMIT);
        final Page<Comment> comments = paginationUtil.paginateList(pageable,
                commentRepository.findByTaskId(taskId));
        final List<CommentDto> commentDtos = comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
        final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(task);
        taskDto.getAssignee().setRoleDtos(getRolesDtos(assignee));
        final CommentResponseDto commentResponseDto = new CommentResponseDto(taskDto, commentDtos);
        sendNotification(userId, task, NEW_COMMENT,
                getBodYText(TOP_BODY_TEXT_NEW_COMMENT, comment));
        return commentResponseDto;
    }

    @Transactional
    @Override
    public Page<CommentResponseDto> getAllComments(TaskSearchParameters searchParameters,
                                                   Pageable pageable) {
        final List<Task> tasksWithComment = taskRepository
                .findAll(taskSpecificationBuilder.build(searchParameters)).stream()
                .filter(t -> !commentRepository.findByTaskId(t.getId()).isEmpty())
                .toList();
        if (tasksWithComment.isEmpty()) {
            throw new EntityNotFoundException("There are no comments by this params for you");
        }
        final List<CommentResponseDto> commentResponseDtos = new ArrayList<>();
        for (Task task : tasksWithComment) {
            final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(task);
            taskDto.getAssignee().setRoleDtos(getRolesDtos(task.getAssignee()));
            final List<Comment> comments = commentRepository.findByTaskId(taskDto.getId());
            List<CommentDto> commentDtos = new ArrayList<>();
            if (comments.size() > COMMENTS_LIMIT) {
                for (int l = ZERO; l < comments.size(); l++) {
                    commentDtos.add(commentMapper.toCommentDto(comments.get(l)));
                    if (commentDtos.size() == COMMENTS_LIMIT || l == comments.size() - 1) {
                        final CommentResponseDto commentResponseDto =
                                new CommentResponseDto(taskDto, commentDtos);
                        commentResponseDtos.add(commentResponseDto);
                        commentDtos = new ArrayList<>();
                    }
                }
            } else {
                commentDtos = comments.stream()
                        .map(commentMapper::toCommentDto)
                        .collect(Collectors.toList());
                commentResponseDtos.add(new CommentResponseDto(taskDto, commentDtos));
            }
        }
        return paginationUtil.paginateList(pageable, commentResponseDtos);
    }

    @Transactional
    @Override
    public void deleteById(Long commentId, Long userId) {
        final Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find comment by id: " + commentId));
        if (!comment.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("You can't remove other users comments");
        }
        commentRepository.deleteById(commentId);
        sendNotification(userId,comment.getTask(), COMMENT_REMOVED,
                getBodYText(BODY_TEXT_COMMENT_REMOVED, comment));
    }

    private Set<String> getRolesDtos(User user) {
        return user.getRoles().stream()
                .map(r -> r.getName().toString())
                .collect(Collectors.toSet());
    }

    private void sendNotification(Long userId, Task task,String subject, String bodyText) {
        final User assignee = task.getAssignee();
        if (userId.equals(assignee.getId())) {
            final Set<User> managers = userRepository.findByRolesId(MANAGER_ROLE_ID);
            final List<String> emails = managers.stream()
                    .filter(u -> !u.getId().equals(ADMIN_ID) && !u.getId().equals(userId))
                    .map(User::getEmail)
                    .toList();
            for (String email : emails) {
                emailService.sendEmail(email, subject, bodyText);
            }
        } else {
            emailService.sendEmail(assignee.getEmail(), subject, bodyText);
        }
    }

    private String getBodYText(String bodyText, Comment comment) {
        return new StringBuilder(bodyText).append(COMMENT_ID).append(comment.getId())
                .append(WAS_WRITTEN).append(comment.getUser().getUsername())
                .append(WITH_USER_ID).append(comment.getUser().getId()).append(FOR_TASK)
                .append(comment.getTask().getName()).append(WITH_TASK_ID).append(comment.getTask()
                        .getId()).append(AT).append(comment.getTimestamp()).toString();
    }
}
