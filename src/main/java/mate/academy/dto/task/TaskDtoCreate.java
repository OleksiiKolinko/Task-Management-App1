package mate.academy.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import mate.academy.annotation.MyDateFormatCheck;

public record TaskDtoCreate(@NotBlank String name, @NotBlank String description,
                            @NotBlank String priority, @NotBlank String status,
                            @MyDateFormatCheck(pattern = "yyyy-MM-dd",
                                    message = "must be in format: yyyy-MM-dd") String dueDate,
                            @Positive Long project, @Positive Long assignee) {
}
