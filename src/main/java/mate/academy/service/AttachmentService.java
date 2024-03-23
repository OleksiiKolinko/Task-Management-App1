package mate.academy.service;

import mate.academy.dto.attachment.AllAttachmentsResponseDto;
import mate.academy.dto.attachment.AttachmentRequestDto;
import mate.academy.dto.attachment.AttachmentResponseDto;
import mate.academy.dto.task.TaskSearchParameters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttachmentService {
    AttachmentResponseDto uploadFile(AttachmentRequestDto requestDto, Long userId);

    Page<AllAttachmentsResponseDto> getAllFiles(
            Pageable pageable, TaskSearchParameters searchParameters, Long userId);

    void deleteById(Long attachmentId, Long userId);

}
