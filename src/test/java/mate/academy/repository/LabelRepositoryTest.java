package mate.academy.repository;

import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.Label;
import mate.academy.model.Task;
import mate.academy.repository.label.LabelRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LabelRepositoryTest {
    private static final Long ONE_ID = 1L;
    private static final int ONE = 1;
    private static final int ZERO = 0;
    private static final String LABEL_NAME = "labelName";
    private static final String COLOR = "color";
    private static final String ADD_DATA_LABEL = "classpath:database/label/add-data-label.sql";
    private static final String ADD_LABEL_WITHOUT_TASKS =
            "classpath:database/label/add-data-label-without-tasks.sql";
    private static final String REMOVE_ALL_LABEL = "classpath:database/label/remove-all-label.sql";
    @Autowired
    private LabelRepository labelRepository;

    @Test
    @DisplayName("Verify findByName() method works")
    @Sql(scripts = ADD_DATA_LABEL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_LABEL, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByName_ValidLabel_returnLabel() {
        final Label label = labelRepository.findByName(LABEL_NAME).orElseThrow(
                () -> new EntityNotFoundException("Can't find label by name labelName"));
        Assertions.assertEquals(ONE_ID, label.getId());
        Assertions.assertEquals(LABEL_NAME, label.getName());
        Assertions.assertEquals(COLOR, label.getColor());
        Assertions.assertEquals(ONE, label.getTasks().size());
        Assertions.assertEquals(ONE_ID, label.getTasks().stream()
                .map(Task::getId)
                .toList().get(ZERO));
    }

    @Test
    @DisplayName("Verify findByName() method works")
    @Sql(scripts = ADD_DATA_LABEL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_LABEL, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByTasksId_ValidLabel_returnLabel() {
        Label actual = labelRepository.findByTasksId(ONE_ID).orElseThrow(
                () -> new EntityNotFoundException("Can't find label by id 1L"));
        Assertions.assertEquals(ONE_ID, actual.getId());
        Assertions.assertEquals(LABEL_NAME, actual.getName());
        Assertions.assertEquals(COLOR, actual.getColor());
        Assertions.assertEquals(ONE, actual.getTasks().size());
        Assertions.assertEquals(ONE_ID, actual.getTasks().stream()
                .map(Task::getId)
                .toList().get(ZERO));
    }

    @Test
    @DisplayName("Verify deleteByTasksIsEmpty() method works")
    @Sql(scripts = ADD_LABEL_WITHOUT_TASKS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void deleteByTasksIsEmpty_Valid_Deleted() {
        Assertions.assertTrue(labelRepository.findById(ONE_ID).isPresent());
        labelRepository.deleteByTasksIsEmpty();
        Assertions.assertTrue(labelRepository.findById(ONE_ID).isEmpty());
    }
}
