package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.project.CreateProjectDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.project.UpdateProjectDto;
import mate.academy.service.ProjectService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Projects management",
        description = "Create a new project, retrieve user's projects, retrieve project details,"
                + " update project and delete project")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @Operation(summary = "Create project",
            description = "Create projects can do only users with ROLE_MANAGER")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseProjectDto createProject(@RequestBody @Valid CreateProjectDto requestDto) {
        return projectService.createProject(requestDto);
    }

    @Operation(summary = "Retrieve user's projects",
            description = "Showing all projects. This allowed for users with ROLE_USER"
                    + " and ROLE_MANAGER")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public List<ResponseProjectDto> getAllProjects(Pageable pageable) {
        return projectService.getAllProjects(pageable);
    }

    @Operation(summary = "Retrieve project details",
            description = "Showing project by particular id. This allowed for users with ROLE_USER"
                    + " and ROLE_MANAGER")
    @GetMapping("/{projectId}")
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_MANAGER')")
    public ResponseProjectDto getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    @Operation(summary = "Update project",
            description = "Update project by particular id."
                    + " This only allowed for users with ROLE_MANAGER")
    @PatchMapping("/{projectId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseProjectDto updateProject(@PathVariable Long projectId,
                                            @RequestBody @Valid UpdateProjectDto requestDto) {
        return projectService.updateProject(projectId, requestDto);
    }

    @Operation(summary = "Delete project",
            description = "Delete project by particular id."
                    + " This only allowed for users with ROLE_MANAGER")
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public void deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
    }
}
