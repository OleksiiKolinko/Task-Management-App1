package mate.academy.dto.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttachmentResponseDownloadDto {
    private Long id;
    private String filename;
    private String uploadDate;
    private String dropboxFileId;
    private String download;
}
