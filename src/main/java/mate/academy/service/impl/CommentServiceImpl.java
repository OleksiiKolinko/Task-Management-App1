package mate.academy.service.impl;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import mate.academy.service.EmailMessageUtil;
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
    private static final int COMMENTS_LIMIT = 50;
    private static final int ZERO = 0;
    private static final String WAS_ADDED = "was added";
    private static final String WAS_DELETED = "was deleted";
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final RoleRepository roleRepository;
    private final CommentRepository commentRepository;
    private final TaskMapper taskMapper;
    private final CommentMapper commentMapper;
    private final PaginationUtil paginationUtil;
    private final TaskSpecificationBuilder taskSpecificationBuilder;
    private final EmailMessageUtil emailMessageUtil;

    @Transactional
    @Override
    public CommentResponseDto save(Long userId, CommentRequestDto requestDto) {
        final Long taskId = requestDto.taskId();
        final Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("Cant find task by id: " + taskId));
        final User assignee = task.getAssignee();
        final User user = getUser(userId, assignee);
        final Comment comment = Comment.builder().task(task).user(user).text(requestDto.text())
                .timestamp(LocalDateTime.now()).build();
        task.getComments().add(comment);
        commentRepository.save(comment);
        final List<Comment> commentList = task.getComments();
        commentList.sort(Comparator.comparing(Comment::getTimestamp).reversed());
        final Page<Comment> comments = paginationUtil.paginateList(
                PageRequest.of(ZERO, COMMENTS_LIMIT), commentList);
        final List<CommentDto> commentDtos = comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(toList());
        final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(task);
        final CommentResponseDto commentResponseDto = new CommentResponseDto(taskDto, commentDtos);
        sendNotification(NEW_COMMENT, WAS_ADDED, comment, user);
        return commentResponseDto;
    }

    @Override
    public Page<CommentResponseDto> getAllComments(TaskSearchParameters searchParameters,
                                                   Pageable pageable) {
        return paginationUtil.paginateList(pageable,
                getCommentResponseDtos(getTasksWithComment(searchParameters)));
    }

    @Override
    public void deleteById(Long commentId, Long userId) {
        final Comment comment = getValidComment(commentId, userId);
        commentRepository.deleteById(commentId);
        sendNotification(COMMENT_REMOVED, WAS_DELETED, comment, comment.getUser());
    }

    private Comment getValidComment(Long commentId, Long userId) {
        final Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find comment by id: " + commentId));
        if (!comment.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("You can't remove other users comments");
        }
        return comment;
    }

    private void sendNotification(String subject, String action, Comment comment, User user) {
        final User assignee = comment.getTask().getAssignee();
        if (user.equals(assignee)) {
            emailMessageUtil.sendAddOrRemoveComment(userRepository.findByRolesId(MANAGER_ROLE_ID)
                    .stream().filter(manager -> !manager.getId().equals(ADMIN_ID)
                    && !manager.equals(user)).map(User::getEmail)
                    .collect(Collectors.toSet()), subject, action, comment, user);
        } else {
            emailMessageUtil.sendAddOrRemoveComment(Set.of(assignee.getEmail()),
                    subject, action, comment, user);
        }
    }

    private User getUser(Long userId, User assignee) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + userId));
        final Role manegerRole = roleRepository.findById(MANAGER_ROLE_ID).orElseThrow(
                () -> new EntityNotFoundException("Cant find role by id: " + MANAGER_ROLE_ID));
        if (!user.getRoles().contains(manegerRole) && !user.equals(assignee)) {
            throw new EntityNotFoundException(
                    "You can comment only yours tasks, or if you have ROLE_MANAGER");
        }
        return user;
    }

    private List<Task> getTasksWithComment(TaskSearchParameters searchParameters) {
        final List<Task> tasksWithComment = taskRepository
                .findAll(taskSpecificationBuilder.build(searchParameters)).stream()
                .filter(task -> !task.getComments().isEmpty())
                .toList();
        if (tasksWithComment.isEmpty()) {
            throw new EntityNotFoundException("There are no comments by this params for you");
        }
        return tasksWithComment;
    }

    private List<CommentResponseDto> getCommentResponseDtos(List<Task> tasksWithComment) {
        List<CommentResponseDto> commentResponseDtos = new ArrayList<>();
        for (Task task : tasksWithComment) {
            final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(task);
            final List<Comment> comments = task.getComments();
            comments.sort(Comparator.comparing(Comment::getTimestamp).reversed());
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
                        .collect(toList());
                commentResponseDtos.add(new CommentResponseDto(taskDto, commentDtos));
            }
        }
        return commentResponseDtos;
    }
}
