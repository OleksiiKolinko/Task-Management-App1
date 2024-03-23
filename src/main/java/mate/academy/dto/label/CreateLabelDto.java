package mate.academy.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Set;

public record CreateLabelDto(@NotBlank String name, @NotBlank String color,
                             Set<@Positive Long> tasks) {
}
