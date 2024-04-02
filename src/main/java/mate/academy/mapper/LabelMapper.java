package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.label.ResponseLabelDto;
import mate.academy.model.Label;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class, uses = UserMapper.class)
public interface LabelMapper {
    ResponseLabelDto toResponseLabelDto(Label label);
}
