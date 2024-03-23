package mate.academy.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaginationUtil {
    <T> Page<T> paginateList(final Pageable pageable, List<T> list);
}
