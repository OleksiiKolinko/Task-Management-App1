package mate.academy.repository;

import java.time.LocalDate;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.Project;
import mate.academy.repository.project.ProjectRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProjectRepositoryTest {
    private static final Long ONE = 1L;
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final LocalDate START_DATE = LocalDate.of(2100, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2200, 1,1);
    private static final String ADD_DATA_PROJECT =
            "classpath:database/project/add-data-project.sql";
    private static final String REMOVE_ALL_PROJECT =
            "classpath:database/project/remove-all-project.sql";
    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("Verify findByName() method works")
    @Sql(scripts = ADD_DATA_PROJECT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_PROJECT, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByName_ValidProject_ReturnProject() {
        final Project actual = projectRepository.findByName(PROJECT_NAME).orElseThrow(
                () -> new EntityNotFoundException("Can't find project by name projectName")
        );
        Assertions.assertEquals(ONE, actual.getId());
        Assertions.assertEquals(PROJECT_NAME, actual.getName());
        Assertions.assertEquals(PROJECT_DESCRIPTION, actual.getDescription());
        Assertions.assertEquals(START_DATE, actual.getStartDate());
        Assertions.assertEquals(END_DATE, actual.getEndDate());
        Assertions.assertEquals(Project.Status.INITIATED, actual.getStatus());
    }
}
