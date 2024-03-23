package mate.academy.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.model.Comment;
import mate.academy.repository.comment.CommentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CommentRepositoryTest {
    private static final Long ONE_ID = 1L;
    private static final Long THREE_ID = 3L;
    private static final int THREE = 3;
    private static final int ZERO = 0;
    private static final String ADD_DATA_COMMENTS =
            "classpath:database/comment/add-data-comments.sql";
    private static final String REMOVE_ALL_COMMENTS =
            "classpath:database/comment/remove-all-comments.sql";
    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("Verify findByTaskId() method works")
    @Sql(scripts = ADD_DATA_COMMENTS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_COMMENTS, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByTaskId_ValidTaskAndComments_returnComments() {
        final List<Comment> actual = commentRepository.findByTaskId(ONE_ID);
        Assertions.assertEquals(THREE, actual.size());
        Assertions.assertEquals(commentRepository.findById(THREE_ID),
                Optional.of(actual.get(ZERO)));

    }
}
