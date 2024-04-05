package mate.academy.dto.label;

import java.util.Set;
import mate.academy.dto.task.ResponseTaskDto;

public record ResponseLabelDto(Long id, String name, String color, Set<ResponseTaskDto> tasks) {
}
