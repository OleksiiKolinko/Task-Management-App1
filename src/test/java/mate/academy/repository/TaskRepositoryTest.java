package mate.academy.repository;

import java.util.Set;
import mate.academy.model.Task;
import mate.academy.repository.task.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskRepositoryTest {
    private static final Long ONE_ID = 1L;
    private static final Long TWO_ID = 2L;
    private static final int ONE = 1;
    private static final int THREE = 3;
    private static final String ADD_DATA_TASKS = "classpath:database/task/add-data-tasks.sql";
    private static final String REMOVE_ALL_TASKS = "classpath:database/task/remove-all-tasks.sql";
    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Verify findAllByAssigneeId() method works")
    @Sql(scripts = ADD_DATA_TASKS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_TASKS, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAllByAssigneeId_ValidAll_ReturnTasks() {
        final Set<Task> actual = taskRepository.findAllByAssigneeId(TWO_ID);
        Assertions.assertEquals(THREE, actual.size());
        Assertions.assertEquals(ONE, actual.stream()
                .filter(t -> t.getId().equals(ONE_ID))
                .toList().size());
        Assertions.assertEquals(ONE, actual.stream()
                .filter(t -> t.getId().equals(TWO_ID))
                .toList().size());
    }
}
