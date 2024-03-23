package mate.academy.repository.attachment;

import java.util.List;
import mate.academy.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    boolean existsByFilename(String fileName);

    List<Attachment> findAllByTaskId(Long taskId);
}
