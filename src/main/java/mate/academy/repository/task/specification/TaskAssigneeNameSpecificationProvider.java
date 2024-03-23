package mate.academy.repository.task.specification;

import java.util.Arrays;
import mate.academy.model.Task;
import mate.academy.repository.SpecificationProviderString;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskAssigneeNameSpecificationProvider implements SpecificationProviderString<Task> {
    private static final String ASSIGNEE = "assignee";
    private static final String USERNAME = "username";

    @Override
    public String getKey() {
        return ASSIGNEE;
    }

    @Override
    public Specification<Task> getSpecificationString(String[] params) {
        return (root, query, criteriaBuilder) -> root.get(ASSIGNEE).get(USERNAME)
                .in(Arrays.stream(params).toArray());
    }
}
