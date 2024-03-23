package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.attachment.AttachmentResponseDownloadDto;
import mate.academy.dto.attachment.AttachmentResponseDto;
import mate.academy.model.Attachment;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface AttachmentMapper {
    AttachmentResponseDto toAttachmentResponseDto(Attachment attachment);

    AttachmentResponseDownloadDto toAttachmentDownloadDto(Attachment a);
}
