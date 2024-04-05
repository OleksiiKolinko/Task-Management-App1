package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.comment.CommentRequestDto;
import mate.academy.dto.comment.CommentResponseDto;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.model.User;
import mate.academy.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comment management",
        description = "Add a new comment, retrieve all comments and delete comment")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "Add comment",
            description = "Add comment can users with ROLE_USER and ROLE_MANAGER")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public CommentResponseDto addComment(Authentication authentication,
                                         @RequestBody @Valid CommentRequestDto requestDto) {
        return commentService.save(getUserId(authentication), requestDto);
    }

    @Operation(summary = "Retrieve comments",
            description = "Showing all comments. Can search by taskIds."
                    + " This allowed for users with ROLE_USER and ROLE_MANAGER")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public Page<CommentResponseDto> getAllComments(TaskSearchParameters searchParameters,
                                                   Pageable pageable) {
        return commentService.getAllComments(searchParameters, pageable);
    }

    @Operation(summary = "Delete comment",
            description = "Delete comment by particular id."
                    + " This allowed for users with ROLE_USER and ROLE_MANAGER")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public void deleteById(@PathVariable Long commentId, Authentication authentication) {
        commentService.deleteById(commentId, getUserId(authentication));
    }

    private Long getUserId(Authentication authentication) {
        final User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
