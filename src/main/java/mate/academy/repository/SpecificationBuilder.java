package mate.academy.repository;

import mate.academy.dto.task.TaskSearchParameters;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> build(TaskSearchParameters searchParameters);
}
