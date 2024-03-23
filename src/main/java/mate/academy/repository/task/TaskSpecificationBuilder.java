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
            spec = spec.and(taskSpecificationProviderManager.getSpecificationProviderLong(ID)
                    .getSpecificationLong(searchParameters.taskIds()));
        }
        if (searchParameters.names() != null && searchParameters.names().length > 0) {
            spec = spec.and(taskSpecificationProviderManager.getSpecificationProviderString(NAME)
                    .getSpecificationString(searchParameters.names()));
        }
        if (searchParameters.projectIds() != null && searchParameters.projectIds().length > 0) {
            spec = spec.and(taskSpecificationProviderManager.getSpecificationProviderLong(PROJECT)
                    .getSpecificationLong(searchParameters.projectIds()));
        }
        if (searchParameters.projectNames() != null && searchParameters.projectNames().length > 0) {
            spec = spec.and(taskSpecificationProviderManager.getSpecificationProviderString(PROJECT)
                    .getSpecificationString(searchParameters.projectNames()));
        }
        if (searchParameters.assigneeIds() != null && searchParameters.assigneeIds().length > 0) {
            spec = spec.and(taskSpecificationProviderManager.getSpecificationProviderLong(ASSIGNEE)
                    .getSpecificationLong(searchParameters.assigneeIds()));
        }
        if (searchParameters.assigneeNames() != null
                && searchParameters.assigneeNames().length > 0) {
            spec = spec
                    .and(taskSpecificationProviderManager.getSpecificationProviderString(ASSIGNEE)
                    .getSpecificationString(searchParameters.assigneeNames()));
        }
        return spec;
    }
}
