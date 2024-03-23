package mate.academy.repository.comment;

import java.util.List;
import mate.academy.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("FROM Comment WHERE task.id = :taskId ORDER BY timestamp DESC")
    List<Comment> findByTaskId(Long taskId);
}
