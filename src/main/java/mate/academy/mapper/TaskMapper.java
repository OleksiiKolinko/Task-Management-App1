package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.model.Task;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface TaskMapper {
    ResponseTaskDto toTaskResponseDto(Task save);
}
