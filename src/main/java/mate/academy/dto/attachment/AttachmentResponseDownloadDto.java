package mate.academy.dto.attachment;

import lombok.Data;

@Data
public class AttachmentResponseDownloadDto {
    private Long id;
    private String filename;
    private String uploadDate;
    private String dropboxFileId;
    private String download;
}
