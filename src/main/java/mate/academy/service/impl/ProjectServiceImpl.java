package mate.academy.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.project.CreateProjectDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.project.UpdateProjectDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.ProjectMapper;
import mate.academy.model.Attachment;
import mate.academy.model.Comment;
import mate.academy.model.Label;
import mate.academy.model.Project;
import mate.academy.model.Task;
import mate.academy.repository.attachment.AttachmentRepository;
import mate.academy.repository.comment.CommentRepository;
import mate.academy.repository.label.LabelRepository;
import mate.academy.repository.project.ProjectRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.service.DropboxService;
import mate.academy.service.EmailService;
import mate.academy.service.ProjectService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private static final String FIRST_PART_BAD_DUE_DATE = "The due date of tasks must be between "
            + "start date and end date of project. If you want update the dates of this project "
            + "you must update due dates for this tasks:";
    private static final String TASK_ID = "task id ";
    private static final String HAVE_DUE_DATE = " have dueDate at this moment ";
    private static final int ZERO = 0;
    private static final String DELETED_TASK = "Deleted task";
    private static final String BODY_TEXT_REMOVED = "Your task removed. The name of task is ";
    private static final int ONE = 1;
    private static final Project.Status DEFAULT_STATUS = Project.Status.INITIATED;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final DropboxService dropboxService;
    private final EmailService emailService;
    private final LabelRepository labelRepository;

    @Override
    public ResponseProjectDto createProject(CreateProjectDto requestDto) {
        final String nameDto = requestDto.name();
        if (projectRepository.findByName(nameDto).isPresent()) {
            throw new EntityNotFoundException("The project with name "
                    + nameDto + " is exist");
        }
        final LocalDate startDateDto = LocalDate.parse(requestDto.startDate());
        final LocalDate nowDate = LocalDate.now();
        if (startDateDto.isBefore(nowDate)) {
            throw new EntityNotFoundException("You can't create project on the previous date."
            + " You put start date " + startDateDto + " but today " + nowDate);
        }
        final Project project = new Project();
        project.setEndDate(getEndDate(LocalDate.parse(requestDto.endDate()), startDateDto));
        project.setName(nameDto);
        project.setDescription(requestDto.description());
        project.setStartDate(startDateDto);
        project.setStatus(DEFAULT_STATUS);
        return projectMapper.toResponseProjectDto(getSave(project, nameDto));
    }

    @Override
    public List<ResponseProjectDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).stream()
                .map(projectMapper::toResponseProjectDto)
                .toList();
    }

    @Override
    public ResponseProjectDto getProject(Long projectId) {
        return projectMapper.toResponseProjectDto(projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("Can't find project by id: " + projectId)));
    }

    @Transactional
    @Override
    public ResponseProjectDto updateProject(Long projectId, UpdateProjectDto requestDto) {
        final Project beforeUpdateProject = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("Can't find project by id: " + projectId));
        final String nameBefore = beforeUpdateProject.getName();
        final String nameDto = requestDto.name();
        if (projectRepository.findByName(nameDto).isPresent() && !nameDto.equals(nameBefore)) {
            throw new EntityNotFoundException("The project with name " + nameDto + " is exist");
        }
        final LocalDate startDateBefore = beforeUpdateProject.getStartDate();
        final LocalDate startDateDto = LocalDate.parse(requestDto.startDate());
        final LocalDate nowDate = LocalDate.now();
        if (startDateDto.isBefore(nowDate) && !startDateDto.isEqual(startDateBefore)) {
            throw new EntityNotFoundException(
                    "Please put correct start date, that is " + startDateBefore
                            + ", or date from this day: " + nowDate);
        }
        final LocalDate endDateDto = getEndDate(LocalDate.parse(requestDto.endDate()),
                startDateDto);
        Set<Task> tasksBadDate = taskRepository.findAllByProjectId(projectId).stream()
                .filter(t -> t.getDueDate().isBefore(startDateDto)
                        || t.getDueDate().isAfter(endDateDto))
                .collect(Collectors.toSet());
        if (!tasksBadDate.isEmpty()) {
            final StringBuilder sum = new StringBuilder();
            final String tasksMustCorrect = tasksBadDate.stream().map(t -> {
                sum.setLength(ZERO);
                return sum.append(System.lineSeparator()).append(TASK_ID).append(t.getId())
                        .append(HAVE_DUE_DATE).append(t.getDueDate());
            }).collect(Collectors.joining());
            sum.setLength(ZERO);
            throw new EntityNotFoundException(sum.append(FIRST_PART_BAD_DUE_DATE)
                    .append(tasksMustCorrect).toString());
        }
        try {
            beforeUpdateProject.setStatus(Project.Status.valueOf(requestDto.status()));
        } catch (RuntimeException e) {
            throw new EntityNotFoundException("The status " + requestDto.status() + " is not exist."
                        + " There are three statuses: INITIATED, IN_PROGRESS, COMPLETED");
        }
        beforeUpdateProject.setName(nameDto);
        beforeUpdateProject.setDescription(requestDto.description());
        beforeUpdateProject.setStartDate(startDateDto);
        beforeUpdateProject.setEndDate(endDateDto);
        return projectMapper.toResponseProjectDto(getSave(beforeUpdateProject, nameDto));
    }

    @Transactional
    @Override
    public void deleteProject(Long projectId) {
        final Set<Task> tasks = taskRepository.findAllByProjectId(projectId);
        final Set<List<Comment>> comments = tasks.stream()
                .map(t -> commentRepository.findByTaskId(t.getId()))
                .collect(Collectors.toSet());
        comments.forEach(cs -> cs.forEach(c -> commentRepository.deleteById(c.getId())));
        final Set<List<Attachment>> attachments = tasks.stream()
                .map(t -> attachmentRepository.findAllByTaskId(t.getId()))
                .collect(Collectors.toSet());
        attachments.forEach(as -> as.forEach(a -> {
            dropboxService.delete(a.getDropboxFileId());
            attachmentRepository.deleteById(a.getId());
        }));
        tasks.forEach(task -> {
            final Long taskId = task.getId();
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
        });
        tasks.forEach(t -> {
            emailService.sendEmail(t.getAssignee().getEmail(), DELETED_TASK,
                    BODY_TEXT_REMOVED + t.getName());
            taskRepository.deleteById(t.getId());
        });
        projectRepository.deleteById(projectId);
    }

    private Project getSave(Project project, String nameDto) {
        try {
            return projectRepository.save(project);
        } catch (RuntimeException e) {
            throw new EntityNotFoundException("The project with name " + nameDto
                    + " used before and then removed by using soft delete concept."
                    + "Please, rename project.");
        }
    }

    private LocalDate getEndDate(LocalDate endDateDto, LocalDate startDateDto) {
        if (endDateDto.isBefore(startDateDto)) {
            throw new EntityNotFoundException("The end date can't be before start date."
                    + " You put end date " + endDateDto + " and start date " + startDateDto);
        }
        return endDateDto;
    }
}
