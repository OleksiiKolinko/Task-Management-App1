package mate.academy.repository.task;

import lombok.RequiredArgsConstructor;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.model.Task;
import mate.academy.repository.SpecificationBuilder;
import mate.academy.repository.SpecificationProviderManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskSpecificationBuilder implements SpecificationBuilder<Task> {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PROJECT = "project";
    private static final String ASSIGNEE = "assignee";
    private final SpecificationProviderManager<Task> taskSpecificationProviderManager;

    @Override
    public Specification<Task> build(TaskSearchParameters searchParameters) {
        Specification<Task> spec = Specification.where(null);
        if (getValidLong(searchParameters.taskIds())) {
            spec = getSpecificationLong(spec, ID, searchParameters.taskIds());
        }
        if (getValidString(searchParameters.names())) {
            spec = getSpecificationString(spec, NAME, searchParameters.names());
        }
        if (getValidLong(searchParameters.projectIds())) {
            spec = getSpecificationLong(spec, PROJECT, searchParameters.projectIds());
        }
        if (getValidString(searchParameters.projectNames())) {
            spec = getSpecificationString(spec, PROJECT, searchParameters.projectNames());
        }
        if (getValidLong(searchParameters.assigneeIds())) {
            spec = getSpecificationLong(spec, ASSIGNEE, searchParameters.assigneeIds());
        }
        if (getValidString(searchParameters.assigneeNames())) {
            spec = getSpecificationString(spec, ASSIGNEE, searchParameters.assigneeNames());
        }
        return spec;
    }

    private Specification<Task> getSpecificationString(Specification<Task> spec, String keyString,
                                                       String[] stringParameters) {
        return spec.and(taskSpecificationProviderManager.getSpecificationProviderString(keyString)
                .getSpecificationString(stringParameters));
    }

    private Specification<Task> getSpecificationLong(Specification<Task> spec,
                                                     String keyLong, Long[] longParameters) {
        return spec.and(taskSpecificationProviderManager.getSpecificationProviderLong(keyLong)
                .getSpecificationLong(longParameters));
    }

    private boolean getValidLong(Long[] params) {
        return params != null && params.length > 0;
    }

    private boolean getValidString(String[] params) {
        return params != null && params.length > 0;
    }
}
