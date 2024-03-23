package mate.academy.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskDtoCreate;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.TaskMapper;
import mate.academy.model.Label;
import mate.academy.model.Project;
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
import mate.academy.service.DropboxService;
import mate.academy.service.EmailService;
import mate.academy.service.TaskService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private static final Long ADMIN_ROLE_ID = 4L;
    private static final Long USER_ROLE_ID = 3L;
    private static final String NEW_TASK = "New task";
    private static final String UPDATE_TASK = "Update task";
    private static final String TEXT_UPDATE_TASK = "Your task updated. The number of task is ";
    private static final String TASK_TRANSFERRED =
            "Your task has been transferred to another person. The number of task is ";
    private static final String RECEIVED_TASK = "You received new task. The number of task is ";
    private static final String TASK_WAS_BEFORE =
            ". The task was before assigned for user with username ";
    private static final String WITH_EMAIL = ", with email ";
    private static final String DELETED_TASK = "Deleted task";
    private static final String BODY_TEXT_REMOVED = "Your task removed. The name of task is ";
    private static final int ONE = 1;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TaskSpecificationBuilder taskSpecificationBuilder;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final DropboxService dropboxService;
    private final LabelRepository labelRepository;

    @Transactional
    @Override
    public ResponseTaskDto createTask(TaskDtoCreate requestDto) {
        final String nameDto = requestDto.name();
        if (taskRepository.findByName(nameDto).isPresent()) {
            throw new EntityNotFoundException("The task with name " + nameDto + " is exist");
        }
        final Project project = getProjectValid(requestDto.project());
        final LocalDate dueDate = getDueDateValid(project.getStartDate(), project.getEndDate(),
                LocalDate.parse(requestDto.dueDate()));
        final LocalDate dateNow = LocalDate.now();
        if (dueDate.isBefore(dateNow)) {
            throw new EntityNotFoundException("You can't create task on the previous date."
                    + " You put dueDate " + dueDate + " but today " + dateNow);
        }
        final User user = getUserValid(requestDto.assignee());
        final Task task = new Task();
        task.setDueDate(dueDate);
        task.setName(nameDto);
        task.setDescription(requestDto.description());
        task.setStatus((getStatusValid(requestDto.status())));
        task.setPriority(getPriorityValid(requestDto.priority()));
        task.setAssignee(user);
        task.setProject(project);
        ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(getSave(task, nameDto));
        taskDto.getAssignee().setRoleDtos(getRoleDtos(user));
        emailService.sendEmail(user.getEmail(), NEW_TASK, RECEIVED_TASK + taskDto.getId());
        return taskDto;
    }

    @Override
    public List<ResponseTaskDto> getAllTasks(TaskSearchParameters searchParameters,
                                             Pageable pageable) {
        return taskRepository.findAll(taskSpecificationBuilder.build(searchParameters), pageable)
                .stream()
                .map(t -> {
                    final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(t);
                    taskDto.getAssignee().setRoleDtos(getRoleDtos(t.getAssignee()));
                    return taskDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ResponseTaskDto getTaskById(Long taskId) {
        final Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("Can't find task by id: " + taskId));
        final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(task);
        taskDto.getAssignee().setRoleDtos(getRoleDtos(task.getAssignee()));
        return taskDto;
    }

    @Transactional
    @Override
    public ResponseTaskDto updateTaskById(Long taskId, TaskDtoCreate requestDto) {
        Task taskBefore = taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("Can't find task by id: " + taskId));
        final String nameUpdate = requestDto.name();
        if (taskRepository.findByName(nameUpdate).isPresent()
                && !nameUpdate.equals(taskBefore.getName())) {
            throw new EntityNotFoundException("The task with name " + nameUpdate + " is exist");
        }
        final Project projectUpdate = getProjectValid(requestDto.project());
        final LocalDate dueDateUpdate = getDueDateValid(projectUpdate.getStartDate(),
                projectUpdate.getEndDate(), LocalDate.parse(requestDto.dueDate()));
        final LocalDate dateNow = LocalDate.now();
        final LocalDate dueDateBefore = taskBefore.getDueDate();
        if (dueDateUpdate.isBefore(dateNow) && !dueDateUpdate.isEqual(dueDateBefore)) {
            throw new EntityNotFoundException("Please put correct date, that is: " + dueDateBefore
                    + " or from this date: " + dateNow);
        }
        final User userBefore = taskBefore.getAssignee();
        final User userUpdate = getUserValid(requestDto.assignee());
        taskBefore.setDueDate(dueDateUpdate);
        taskBefore.setName(nameUpdate);
        taskBefore.setDescription(requestDto.description());
        taskBefore.setStatus(getStatusValid(requestDto.status()));
        taskBefore.setPriority(getPriorityValid(requestDto.priority()));
        taskBefore.setAssignee(userUpdate);
        taskBefore.setProject(projectUpdate);
        final Task taskSaved = getSave(taskBefore, nameUpdate);
        final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(taskSaved);
        taskDto.getAssignee().setRoleDtos(getRoleDtos(taskSaved.getAssignee()));
        sendEmail(userBefore, userUpdate, taskDto.getId());
        return taskDto;
    }

    @Transactional
    @Override
    public void deleteById(Long taskId) {
        final Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new EntityNotFoundException("Can't find task by id " + taskId));
        commentRepository.findByTaskId(taskId)
                .forEach(c -> commentRepository.deleteById(c.getId()));
        attachmentRepository.findAllByTaskId(taskId).forEach(a -> {
            dropboxService.delete(a.getDropboxFileId());
            attachmentRepository.deleteById(a.getId());
        });
        emailService.sendEmail(task.getAssignee().getEmail(), DELETED_TASK,
                BODY_TEXT_REMOVED + task.getName());
        final Optional<Label> label = labelRepository.findByTasksId(taskId);
        if (label.isPresent()) {
            if (label.get().getTasks().size() > ONE) {
                label.get().setTasks(label.get().getTasks().stream()
                        .filter(t -> !t.equals(task))
                        .collect(Collectors.toSet()));
                labelRepository.save(label.get());
            } else {
                labelRepository.delete(label.get());
            }
        }
        taskRepository.deleteById(taskId);
    }

    private Set<String> getRoleDtos(User assignee) {
        return assignee.getRoles().stream()
                .map(r -> r.getName().toString())
                .collect(Collectors.toSet());
    }

    private Task getSave(Task task, String nameDto) {
        try {
            return taskRepository.save(task);
        } catch (RuntimeException e) {
            throw new EntityNotFoundException("The task with name " + nameDto
                    + " used before and then removed by using soft delete concept."
                    + "Please, rename task.");
        }
    }

    private Project getProjectValid(Long projectIdDto) {
        return projectRepository.findById(projectIdDto).orElseThrow(
                () -> new EntityNotFoundException("Can't find project by id: " + projectIdDto));
    }

    private LocalDate getDueDateValid(LocalDate startDate, LocalDate endDate, LocalDate dueDate) {
        if (dueDate.isBefore(startDate) || dueDate.isAfter(endDate)) {
            throw new EntityNotFoundException("Due date can't be before start date of project "
                    + "that is: " + startDate + " or after end date of project that is: "
                    + endDate);
        }
        return dueDate;
    }

    private User getUserValid(Long userIdDto) {
        final User user = userRepository.findById(userIdDto).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + userIdDto));
        if (!user.getRoles().contains(roleRepository.findById(USER_ROLE_ID).orElseThrow(
                () -> new EntityNotFoundException("Can't find role by id: " + USER_ROLE_ID)))) {
            throw new EntityNotFoundException("You can't give task for person without ROLE_USER");
        }
        if (user.getRoles().contains(roleRepository.findById(ADMIN_ROLE_ID).orElseThrow(
                () -> new EntityNotFoundException("Can't find role by id: " + ADMIN_ROLE_ID)))) {
            throw new EntityNotFoundException("You can't give task for person with ROLE_ADMIN");
        }
        return user;
    }

    private Task.Status getStatusValid(String statusDto) {
        try {
            return Task.Status.valueOf(statusDto);
        } catch (RuntimeException e) {
            throw new EntityNotFoundException("The status with name " + statusDto
                    + " is not exist. There three statuses: NOT_STARTED, IN_PROGRESS, COMPLETED");
        }
    }

    private Task.Priority getPriorityValid(String priorityDto) {
        try {
            return Task.Priority.valueOf(priorityDto);
        } catch (Exception e) {
            throw new EntityNotFoundException("The priority with name " + priorityDto
                    + " is not exist. There three priorities: LOW, MEDIUM and HIGH");
        }
    }

    private void sendEmail(User userBefore, User userUpdate, Long taskId) {
        if (userBefore.equals(userUpdate)) {
            emailService.sendEmail(userUpdate.getEmail(), UPDATE_TASK, TEXT_UPDATE_TASK + taskId);
        } else {
            final String userEmailBefore = userBefore.getEmail();
            emailService.sendEmail(userEmailBefore, UPDATE_TASK, TASK_TRANSFERRED + taskId);
            emailService.sendEmail(userUpdate.getEmail(), UPDATE_TASK, RECEIVED_TASK + taskId
                    + TASK_WAS_BEFORE + userBefore.getUsername() + WITH_EMAIL + userEmailBefore);
        }
    }
}
