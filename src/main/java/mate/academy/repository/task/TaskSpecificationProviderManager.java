package mate.academy.repository.task;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.Task;
import mate.academy.repository.SpecificationProviderLong;
import mate.academy.repository.SpecificationProviderManager;
import mate.academy.repository.SpecificationProviderString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskSpecificationProviderManager implements SpecificationProviderManager<Task> {
    private final List<SpecificationProviderLong<Task>> taskSpecificationProvidersLong;
    private final List<SpecificationProviderString<Task>> taskSpecificationProvidersString;

    @Override
    public SpecificationProviderLong<Task> getSpecificationProviderLong(String key) {
        return taskSpecificationProvidersLong.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find correct long specification provider for key " + key));
    }

    @Override
    public SpecificationProviderString<Task> getSpecificationProviderString(String key) {
        return taskSpecificationProvidersString.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find correct string specification provider for key " + key));
    }
}
