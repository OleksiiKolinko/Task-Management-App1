package mate.academy.service;

import java.util.List;
import mate.academy.dto.project.CreateProjectDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.project.UpdateProjectDto;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ResponseProjectDto createProject(CreateProjectDto requestDto);

    List<ResponseProjectDto> getAllProjects(Pageable pageable);

    ResponseProjectDto getProject(Long projectId);

    ResponseProjectDto updateProject(Long projectId, UpdateProjectDto requestDto);

    void deleteProject(Long projectId);
}
