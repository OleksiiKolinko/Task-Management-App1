package mate.academy.repository.label;

import java.util.Optional;
import mate.academy.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByName(String name);

    Optional<Label> findByTasksId(Long taskId);

    void deleteByTasksIsEmpty();
}
