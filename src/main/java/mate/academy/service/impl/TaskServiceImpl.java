package mate.academy.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskDtoCreate;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.exception.EntityNotFoundException;
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
import mate.academy.service.DropboxService;
import mate.academy.service.EmailMessageUtil;
import mate.academy.service.TaskService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private static final Long ADMIN_ROLE_ID = 4L;
    private static final Long USER_ROLE_ID = 3L;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TaskSpecificationBuilder taskSpecificationBuilder;
    private final DropboxService dropboxService;
    private final LabelRepository labelRepository;
    private final EmailMessageUtil emailMessageUtil;

    @Override
    public ResponseTaskDto createTask(TaskDtoCreate requestDto) {
        final User user = getValidUser(requestDto.assignee());
        final String nameDto = getValidName(requestDto.name());
        final Project project = getValidProject(requestDto.project());
        final LocalDate dueDate = getValidDueDate(project.getStartDate(), project.getEndDate(),
                LocalDate.parse(requestDto.dueDate()));
        final Task task = Task.builder().dueDate(checkDueDate(dueDate)).name(nameDto)
                .description(requestDto.description()).status(Task.Status.valueOf(
                        requestDto.status())).priority(Task.Priority.valueOf(
                                requestDto.priority())).assignee(user).project(project).build();
        final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(getSave(task, nameDto));
        emailMessageUtil.sendNewTask(user.getEmail(), taskDto.getId());
        return taskDto;
    }

    @Override
    public List<ResponseTaskDto> getAllTasks(TaskSearchParameters searchParameters,
                                             Pageable pageable) {
        return taskRepository.findAll(taskSpecificationBuilder.build(searchParameters), pageable)
                .stream()
                .map(taskMapper::toTaskResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseTaskDto getTaskById(Long taskId) {
        return taskMapper.toTaskResponseDto(getTask(taskId));
    }

    @Override
    public ResponseTaskDto updateTaskById(Long taskId, TaskDtoCreate requestDto) {
        Task taskBefore = getTask(taskId);
        final String nameUpdate = updateValidName(requestDto.name(), taskId);
        final Project projectUpdate = getValidProject(requestDto.project());
        final LocalDate dueDateUpdate = getValidDueDate(projectUpdate.getStartDate(),
                projectUpdate.getEndDate(), LocalDate.parse(requestDto.dueDate()));
        final User userBefore = taskBefore.getAssignee();
        final User userUpdate = getValidUser(requestDto.assignee());
        taskBefore.setDueDate(checkUpdateDueDate(dueDateUpdate, taskBefore.getDueDate()));
        taskBefore.setName(nameUpdate);
        taskBefore.setDescription(requestDto.description());
        taskBefore.setStatus(Task.Status.valueOf(requestDto.status()));
        taskBefore.setPriority(Task.Priority.valueOf(requestDto.priority()));
        taskBefore.setAssignee(userUpdate);
        taskBefore.setProject(projectUpdate);
        final Task taskSaved = getSave(taskBefore, nameUpdate);
        final ResponseTaskDto taskDto = taskMapper.toTaskResponseDto(taskSaved);
        emailMessageUtil.sendUpdateTask(userBefore, userUpdate, taskDto.getId());
        return taskDto;
    }

    @Transactional
    @Override
    public void deleteById(Long taskId) {
        final Task task = getTask(taskId);
        taskRepository.deleteById(taskId);
        deleteFilesLabelIfTasksIsEmptyAndSendEmail(task);
    }

    @Override
    public void deleteFilesLabelIfTasksIsEmptyAndSendEmail(Task task) {
        task.getAttachments().forEach(attachment -> dropboxService
                .delete(attachment.getDropboxFileId()));
        emailMessageUtil.sendDeleteTask(task);
        labelRepository.deleteByTasksIsEmpty();
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

    private Project getValidProject(Long projectIdDto) {
        return projectRepository.findById(projectIdDto).orElseThrow(
                () -> new EntityNotFoundException("Can't find project by id: " + projectIdDto));
    }

    private LocalDate getValidDueDate(LocalDate startDate, LocalDate endDate, LocalDate dueDate) {
        if (dueDate.isBefore(startDate) || dueDate.isAfter(endDate)) {
            throw new EntityNotFoundException("Due date can't be before start date of project "
                    + "that is: " + startDate + " or after end date of project that is: "
                    + endDate);
        }
        return dueDate;
    }

    private User getValidUser(Long userIdDto) {
        final User user = userRepository.findById(userIdDto).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + userIdDto));
        final Set<Role> role = user.getRoles();
        if (!role.contains(getRoleById(USER_ROLE_ID))) {
            throw new EntityNotFoundException("You can't give task for person without ROLE_USER");
        }
        if (role.contains(getRoleById(ADMIN_ROLE_ID))) {
            throw new EntityNotFoundException("You can't give task for person with ROLE_ADMIN");
        }
        return user;
    }

    private String getValidName(String name) {
        taskRepository.findByName(name).ifPresent((foudTask) -> {
            throw new EntityNotFoundException("The task with name " + name + " is exist");
        });
        return name;
    }

    private String updateValidName(String nameDto, Long taskIdBefore) {
        taskRepository.findByName(nameDto).ifPresent((foundTask) -> {
            if (!taskIdBefore.equals(foundTask.getId())) {
                throw new EntityNotFoundException("The task with name " + nameDto + " is exist");
            }
        });
        return nameDto;
    }

    private LocalDate checkDueDate(LocalDate dueDate) {
        final LocalDate dateNow = LocalDate.now();
        if (dueDate.isBefore(dateNow)) {
            throw new EntityNotFoundException("You can't create task on the previous date."
                    + " You put dueDate " + dueDate + " but today " + dateNow);
        }
        return dueDate;
    }

    private LocalDate checkUpdateDueDate(LocalDate dueDateUpdate, LocalDate dueDateBefore) {
        final LocalDate dateNow = LocalDate.now();
        if (dueDateUpdate.isBefore(dateNow) && !dueDateUpdate.isEqual(dueDateBefore)) {
            throw new EntityNotFoundException("Please put correct date, that is: " + dueDateBefore
                    + " or from this date: " + dateNow);
        }
        return dueDateUpdate;
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("Can't find task by id: " + taskId));
    }

    private Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId).orElseThrow(
                () -> new EntityNotFoundException("Can't find role by id: " + roleId));
    }
}
