package mate.academy.repository.task.specification;

import java.util.Arrays;
import mate.academy.model.Task;
import mate.academy.repository.SpecificationProviderString;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskProjectNameSpecificationProvider implements SpecificationProviderString<Task> {
    private static final String PROJECT = "project";
    private static final String NAME = "name";

    @Override
    public String getKey() {
        return PROJECT;
    }

    @Override
    public Specification<Task> getSpecificationString(String[] params) {
        return (root, query, criteriaBuilder) -> root.get(PROJECT).get(NAME)
                .in(Arrays.stream(params).toArray());
    }
}
