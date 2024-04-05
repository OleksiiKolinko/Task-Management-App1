package mate.academy.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.project.CreateProjectDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.project.UpdateProjectDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.ProjectMapper;
import mate.academy.model.Project;
import mate.academy.model.Task;
import mate.academy.repository.project.ProjectRepository;
import mate.academy.service.ProjectService;
import mate.academy.service.TaskService;
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
    private static final Project.Status DEFAULT_STATUS = Project.Status.INITIATED;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final TaskService taskService;

    @Override
    public ResponseProjectDto createProject(CreateProjectDto requestDto) {
        final String nameDto = getValidName(requestDto.name());
        final LocalDate startDateDto = getValidStartDate(LocalDate.parse(requestDto.startDate()));
        final Project project = Project.builder().endDate(
                getEndDate(LocalDate.parse(requestDto.endDate()), startDateDto)).name(nameDto)
                .description(requestDto.description()).startDate(startDateDto)
                .status(DEFAULT_STATUS).build();
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
        return projectMapper.toResponseProjectDto(getProjectById(projectId));
    }

    @Override
    public ResponseProjectDto updateProject(Long projectId, UpdateProjectDto requestDto) {
        final Project beforeUpdateProject = getProjectById(projectId);
        final String nameDto = updateValidName(requestDto.name(), projectId);
        final LocalDate startDateDto = updateValidStartDate(LocalDate.parse(requestDto.startDate()),
                beforeUpdateProject.getStartDate());
        final LocalDate endDateDto = getEndDate(LocalDate.parse(requestDto.endDate()),
                startDateDto);
        checkTasks(startDateDto, endDateDto, beforeUpdateProject);
        beforeUpdateProject.setStatus(Project.Status.valueOf(requestDto.status()));
        beforeUpdateProject.setName(nameDto);
        beforeUpdateProject.setDescription(requestDto.description());
        beforeUpdateProject.setStartDate(startDateDto);
        beforeUpdateProject.setEndDate(endDateDto);
        return projectMapper.toResponseProjectDto(getSave(beforeUpdateProject, nameDto));
    }

    @Transactional
    @Override
    public void deleteProject(Long projectId) {
        final Set<Task> tasks = getProjectById(projectId).getTasks();
        projectRepository.deleteById(projectId);
        tasks.forEach(taskService::deleteFilesLabelIfTasksIsEmptyAndSendEmail);
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

    private String getValidName(String name) {
        projectRepository.findByName(name).ifPresent((foundProject) -> {
            throw new EntityNotFoundException("The project with name "
                    + name + " is exist");
        });
        return name;
    }

    private LocalDate getValidStartDate(LocalDate startDate) {
        final LocalDate nowDate = LocalDate.now();
        if (startDate.isBefore(nowDate)) {
            throw new EntityNotFoundException("You can't create project on the previous date."
                    + " You put start date " + startDate + " but today " + nowDate);
        }
        return startDate;
    }

    private String updateValidName(String name, Long projectId) {
        projectRepository.findByName(name).ifPresent((foundProject) -> {
            if (!foundProject.getId().equals(projectId)) {
                throw new EntityNotFoundException("The project with name " + name + " is exist");
            }
        });
        return name;
    }

    private LocalDate updateValidStartDate(LocalDate startDateDto, LocalDate startDateBefore) {
        final LocalDate nowDate = LocalDate.now();
        if (startDateDto.isBefore(nowDate) && !startDateDto.isEqual(startDateBefore)) {
            throw new EntityNotFoundException(
                    "Please put correct start date, that is " + startDateBefore
                            + ", or date from this day: " + nowDate);
        }
        return startDateDto;
    }

    private void checkTasks(LocalDate startDateDto, LocalDate endDateDto, Project project) {
        final Set<Task> tasksBadDate = project.getTasks().stream()
                .filter(task -> task.getDueDate().isBefore(startDateDto)
                        || task.getDueDate().isAfter(endDateDto))
                .collect(Collectors.toSet());
        if (!tasksBadDate.isEmpty()) {
            final StringBuilder sum = new StringBuilder();
            final String tasksMustCorrect = tasksBadDate.stream().map(taskBadDate -> {
                sum.setLength(ZERO);
                return sum.append(System.lineSeparator()).append(TASK_ID)
                        .append(taskBadDate.getId()).append(HAVE_DUE_DATE)
                        .append(taskBadDate.getDueDate());
            }).collect(Collectors.joining());
            sum.setLength(ZERO);
            throw new EntityNotFoundException(sum.append(FIRST_PART_BAD_DUE_DATE)
                    .append(tasksMustCorrect).toString());
        }
    }

    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("Can't find project by id: " + projectId));
    }
}
