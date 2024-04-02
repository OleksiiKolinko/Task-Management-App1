package mate.academy.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import mate.academy.annotation.MyDateFormatCheck;
import mate.academy.annotation.ValueOfEnum;
import mate.academy.model.Task;

public record TaskDtoCreate(@NotBlank String name, @NotBlank String description,
                            @ValueOfEnum(enumClass = Task.Priority.class,
                                    message = "Wrong priority. There three priorities: "
                                            + "LOW, MEDIUM and HIGH") String priority,
                            @ValueOfEnum(enumClass = Task.Status.class,
                                    message = "Wrong status. There three statuses: NOT_STARTED, "
                                            + "IN_PROGRESS, COMPLETED") String status,
                            @MyDateFormatCheck(pattern = "yyyy-MM-dd",
                                    message = "must be in format: yyyy-MM-dd") String dueDate,
                            @Positive Long project, @Positive Long assignee) {
}
