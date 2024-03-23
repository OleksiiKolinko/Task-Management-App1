package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.label.CreateLabelDto;
import mate.academy.dto.label.ResponseLabelDto;
import mate.academy.service.LabelService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Label management",
        description = "Add a new label, retrieve all labels, update label and delete label")
@RestController
@RequestMapping("/labels")
@RequiredArgsConstructor
public class LabelController {
    private final LabelService labelService;

    @Operation(summary = "Create label",
            description = "To create label can users with ROLE_MANAGER")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseLabelDto createLabel(@RequestBody @Valid CreateLabelDto createLabelDto) {
        return labelService.createLabel(createLabelDto);
    }

    @Operation(summary = "Retrieve labels",
            description = "Showing all labels. This allowed for users with ROLE_USER")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<ResponseLabelDto> findAll(Pageable pageable) {
        return labelService.findAll(pageable);
    }

    @Operation(summary = "Update label",
            description = "Update label by particular id."
                    + " This only allowed for users with ROLE_MANAGER")
    @PutMapping("/{labelId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseLabelDto updateById(@PathVariable Long labelId,
                                       @RequestBody @Valid CreateLabelDto createLabelDto) {
        return labelService.updateById(labelId, createLabelDto);
    }

    @Operation(summary = "Delete label",
            description = "Delete label by particular id."
                    + " This only allowed for users with ROLE_MANAGER")
    @DeleteMapping("/{labelId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public void deleteById(@PathVariable Long labelId) {
        labelService.deleteById(labelId);
    }
}
