package mate.academy.dto.project;

public record ResponseProjectDto(Long id, String name, String description,
                                 String startDate, String endDate, String status) {
}
