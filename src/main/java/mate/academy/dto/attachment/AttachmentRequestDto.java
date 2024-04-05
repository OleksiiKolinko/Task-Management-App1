package mate.academy.dto.attachment;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public record AttachmentRequestDto(@Positive @RequestParam("taskId") Long taskId,
                                   @RequestParam("file") MultipartFile file) {
}
