package mate.academy.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.model.Attachment;
import mate.academy.repository.attachment.AttachmentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AttachmentRepositoryTest {
    private static final Long ONE_ID = 1L;
    private static final int TWO = 2;
    private static final int ZERO = 0;
    private static final String FILE_NAME = "filename";
    private static final String ADD_DATA_EXISTS =
            "classpath:database/attachment/add-data-exists.sql";
    private static final String REMOVE_ALL_DATA =
            "classpath:database/attachment/remove-all-data.sql";
    @Autowired
    private AttachmentRepository attachmentRepository;

    @Test
    @DisplayName("Verify existsByFilename() method works")
    @Sql(scripts = ADD_DATA_EXISTS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_DATA, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void existsByFilename_AttachmentExisted_true() {
        Assertions.assertTrue(attachmentRepository.existsByFilename(FILE_NAME));
    }

    @Test
    @DisplayName("Verify findAllByTaskId() method works")
    @Sql(scripts = ADD_DATA_EXISTS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_DATA, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAllByTaskId_ValidAttachments_returnAttachments() {
        final List<Attachment> actual = attachmentRepository.findAllByTaskId(ONE_ID);
        Assertions.assertEquals(TWO, actual.size());
        Assertions.assertEquals(attachmentRepository.findById(ONE_ID),
                Optional.of(actual.get(ZERO)));
    }
}
