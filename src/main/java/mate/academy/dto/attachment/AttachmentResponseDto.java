package mate.academy.dto.attachment;

import mate.academy.dto.task.ResponseTaskDto;

public record AttachmentResponseDto(Long id, ResponseTaskDto task, String dropboxFileId,
                                    String filename, String uploadDate) {
}
