package mate.academy.repository.task;

import java.util.Optional;
import java.util.Set;
import mate.academy.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findByName(String name);

    Set<Task> findAllByProjectId(Long projectId);

    Set<Task> findAllByAssigneeId(Long userId);
}
