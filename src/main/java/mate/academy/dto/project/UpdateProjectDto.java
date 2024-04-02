package mate.academy.dto.project;

import jakarta.validation.constraints.NotBlank;
import mate.academy.annotation.MyDateFormatCheck;
import mate.academy.annotation.ValueOfEnum;
import mate.academy.model.Project;

public record UpdateProjectDto(@NotBlank String name,
                               @NotBlank String description,
                               @MyDateFormatCheck(pattern = "yyyy-MM-dd",
                                       message = "must be in format: yyyy-MM-dd") String startDate,
                               @MyDateFormatCheck(pattern = "yyyy-MM-dd",
                                       message = "must be in format: yyyy-MM-dd") String endDate,
                               @ValueOfEnum(enumClass = Project.Status.class, message =
                                       "Wrong status. There are three statuses: "
                                               + "INITIATED, IN_PROGRESS, COMPLETED")
                               String status) {
}
