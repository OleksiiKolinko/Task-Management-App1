package mate.academy.service;

import java.util.List;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskDtoCreate;
import mate.academy.dto.task.TaskSearchParameters;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    ResponseTaskDto createTask(TaskDtoCreate requestDto);

    List<ResponseTaskDto> getAllTasks(TaskSearchParameters searchParameters, Pageable pageable);

    ResponseTaskDto getTaskById(Long taskId);

    ResponseTaskDto updateTaskById(Long taskId, TaskDtoCreate requestDto);

    void deleteById(Long taskId);
}
