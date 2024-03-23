package mate.academy.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CommentRequestDto(@Positive Long taskId, @NotBlank String text) {
}
