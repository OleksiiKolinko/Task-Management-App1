package mate.academy.service;

import java.util.List;
import mate.academy.dto.label.CreateLabelDto;
import mate.academy.dto.label.ResponseLabelDto;
import org.springframework.data.domain.Pageable;

public interface LabelService {
    ResponseLabelDto createLabel(CreateLabelDto createLabelDto);

    List<ResponseLabelDto> findAll(Pageable pageable);

    ResponseLabelDto updateById(Long labelId, CreateLabelDto createLabelDto);

    void deleteById(Long labelId);
}
