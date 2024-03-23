package mate.academy.service;

import mate.academy.dto.comment.CommentRequestDto;
import mate.academy.dto.comment.CommentResponseDto;
import mate.academy.dto.task.TaskSearchParameters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponseDto save(Long userId, CommentRequestDto requestDto);

    Page<CommentResponseDto> getAllComments(TaskSearchParameters searchParameters,
                                            Pageable pageable);

    void deleteById(Long commentId, Long userId);
}
