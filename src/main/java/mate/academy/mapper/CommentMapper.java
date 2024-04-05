package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.comment.CommentDto;
import mate.academy.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CommentMapper {
    @Mapping(target = "username", source = "user.username")
    CommentDto toCommentDto(Comment comment);
}
