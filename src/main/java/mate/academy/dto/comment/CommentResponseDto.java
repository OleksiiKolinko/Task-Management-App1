package mate.academy.dto.comment;

import java.util.List;
import mate.academy.dto.task.ResponseTaskDto;

public record CommentResponseDto(ResponseTaskDto taskDto,
                                 List<CommentDto> commentDtos) {
}
