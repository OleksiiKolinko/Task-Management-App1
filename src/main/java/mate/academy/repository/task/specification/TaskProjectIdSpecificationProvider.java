package mate.academy.repository.task.specification;

import java.util.Arrays;
import mate.academy.model.Task;
import mate.academy.repository.SpecificationProviderLong;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskProjectIdSpecificationProvider implements SpecificationProviderLong<Task> {
    private static final String PROJECT = "project";
    private static final String ID = "id";

    @Override
    public String getKey() {
        return PROJECT;
    }

    @Override
    public Specification<Task> getSpecificationLong(Long[] params) {
        return (root, query, criteriaBuilder) -> root.get(PROJECT).get(ID)
                .in(Arrays.stream(params).toArray());
    }
}
