package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskDtoCreate;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.service.TaskService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Task management",
        description = "Create a new task, retrieve all tasks, retrieve task details,"
                + " update task and delete task")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "Create task",
            description = "Create tasks can do only users with ROLE_MANAGER")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseTaskDto createTask(@RequestBody @Valid TaskDtoCreate requestDto) {
        return taskService.createTask(requestDto);
    }

    @Operation(summary = "Retrieve tasks",
            description = "Showing all task. This allowed for users with ROLE_USER "
                    + "and ROLE_MANAGER")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public List<ResponseTaskDto> getAllTasks(TaskSearchParameters searchParameters,
                                             Pageable pageable) {
        return taskService.getAllTasks(searchParameters, pageable);
    }

    @Operation(summary = "Retrieve task details",
            description = "Showing task by particular id. This allowed for users with ROLE_USER"
                    + " and ROLE_MANAGER")
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public ResponseTaskDto getTaskById(@PathVariable Long taskId) {
        return taskService.getTaskById(taskId);
    }

    @Operation(summary = "Update task",
            description = "Update task by particular id."
                    + " This only allowed for users with ROLE_MANAGER")
    @PatchMapping("/{taskId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseTaskDto updateTaskById(@PathVariable Long taskId,
                                          @RequestBody @Valid TaskDtoCreate requestDto) {
        return taskService.updateTaskById(taskId, requestDto);
    }

    @Operation(summary = "Delete task",
            description = "Delete task by particular id."
                    + " This only allowed for users with ROLE_MANAGER")
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public void deleteById(@PathVariable Long taskId) {
        taskService.deleteById(taskId);
    }
}
