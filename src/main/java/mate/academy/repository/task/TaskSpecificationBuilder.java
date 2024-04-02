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
        if (searchParameters.taskIds() != null && searchParameters.taskIds().length > 0) {
            spec = getSpecificationLong(spec, ID, searchParameters.taskIds());
        }
        if (searchParameters.names() != null && searchParameters.names().length > 0) {
            spec = getSpecificationString(spec, NAME, searchParameters.names());
        }
        if (searchParameters.projectIds() != null && searchParameters.projectIds().length > 0) {
            spec = getSpecificationLong(spec, PROJECT, searchParameters.projectIds());
        }
        if (searchParameters.projectNames() != null && searchParameters.projectNames().length > 0) {
            spec = getSpecificationString(spec, PROJECT, searchParameters.projectNames());
        }
        if (searchParameters.assigneeIds() != null && searchParameters.assigneeIds().length > 0) {
            spec = getSpecificationLong(spec, ASSIGNEE, searchParameters.assigneeIds());
        }
        if (searchParameters.assigneeNames() != null
                && searchParameters.assigneeNames().length > 0) {
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
}
