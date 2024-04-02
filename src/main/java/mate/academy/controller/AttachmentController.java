package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.attachment.AllAttachmentsResponseDto;
import mate.academy.dto.attachment.AttachmentRequestDto;
import mate.academy.dto.attachment.AttachmentResponseDto;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.model.User;
import mate.academy.service.AttachmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Attachment management",
        description = "Upload a new attachment, retrieve all attachments and delete attachment")
@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    @Operation(summary = "Upload comment",
            description = "Upload comment can do users with ROLE_USER and ROLE_MANAGER")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public AttachmentResponseDto uploadFile(@Valid AttachmentRequestDto requestDto,
                                            Authentication authentication) {
        return attachmentService.uploadFile(requestDto, getUserId(authentication));
    }

    @Operation(summary = "Retrieve attachments",
            description = "Show all attachments with downloads links. Can search by task."
                    + " This allowed for users with ROLE_USER and ROLE_MANAGER")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public Page<AllAttachmentsResponseDto> getAllFiles(Pageable pageable,
            Authentication authentication, TaskSearchParameters searchParameters) {
        return attachmentService.getAllFiles(pageable, searchParameters, getUserId(authentication));
    }

    @Operation(summary = "Delete attachment",
            description = "Delete attachment by particular id."
                    + " This allowed for users with ROLE_USER and ROLE_MANAGER")
    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public void deleteById(@PathVariable Long attachmentId, Authentication authentication) {
        attachmentService.deleteById(attachmentId, getUserId(authentication));
    }

    private Long getUserId(Authentication authentication) {
        final User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
