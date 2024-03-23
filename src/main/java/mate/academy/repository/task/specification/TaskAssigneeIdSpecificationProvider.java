package mate.academy.repository.task.specification;

import java.util.Arrays;
import mate.academy.model.Task;
import mate.academy.repository.SpecificationProviderLong;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskAssigneeIdSpecificationProvider implements SpecificationProviderLong<Task> {
    private static final String ASSIGNEE = "assignee";
    private static final String ID = "id";

    @Override
    public String getKey() {
        return ASSIGNEE;
    }

    @Override
    public Specification<Task> getSpecificationLong(Long[] params) {
        return (root, query, criteriaBuilder) -> root.get(ASSIGNEE).get(ID)
                .in(Arrays.stream(params).toArray());
    }
}
