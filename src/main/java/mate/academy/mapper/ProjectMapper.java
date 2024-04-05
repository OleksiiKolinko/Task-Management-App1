package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.model.Project;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface ProjectMapper {
    ResponseProjectDto toResponseProjectDto(Project project);
}
