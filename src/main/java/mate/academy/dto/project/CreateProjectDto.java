package mate.academy.dto.project;

import jakarta.validation.constraints.NotBlank;
import mate.academy.annotation.MyDateFormatCheck;

public record CreateProjectDto(@NotBlank String name,
                               @NotBlank String description,
                               @MyDateFormatCheck(pattern = "yyyy-MM-dd",
                                       message = "must be in format: yyyy-MM-dd") String startDate,
                               @MyDateFormatCheck(pattern = "yyyy-MM-dd",
                                       message = "must be in format: yyyy-MM-dd") String endDate) {
}
