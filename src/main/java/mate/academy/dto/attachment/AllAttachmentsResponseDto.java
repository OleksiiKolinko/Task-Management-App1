package mate.academy.dto.attachment;

import java.util.List;
import mate.academy.dto.task.ResponseTaskDto;

public record AllAttachmentsResponseDto(ResponseTaskDto taskDto,
                                        List<AttachmentResponseDownloadDto> attachments) {
}
