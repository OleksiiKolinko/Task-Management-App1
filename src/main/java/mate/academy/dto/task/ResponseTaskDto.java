package mate.academy.dto.task;

import lombok.Data;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.user.UserResponseDtoWithRole;

@Data
public class ResponseTaskDto {
    private Long id;
    private String name;
    private String description;
    private String priority;
    private String status;
    private String dueDate;
    private ResponseProjectDto project;
    private UserResponseDtoWithRole assignee;
}
