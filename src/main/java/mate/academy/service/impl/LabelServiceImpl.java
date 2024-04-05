package mate.academy.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.label.CreateLabelDto;
import mate.academy.dto.label.ResponseLabelDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.LabelMapper;
import mate.academy.model.Label;
import mate.academy.model.Task;
import mate.academy.repository.label.LabelRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.service.LabelService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;
    private final TaskRepository taskRepository;

    @Override
    public ResponseLabelDto createLabel(CreateLabelDto createLabelDto) {
        final String name = getValidName(createLabelDto.name());
        final Label label = Label.builder().name(name).color(createLabelDto.color())
                .tasks(getValidTasks(createLabelDto.tasks())).build();
        return labelMapper.toResponseLabelDto(getSave(label, name));
    }

    @Override
    public List<ResponseLabelDto> findAll(Pageable pageable) {
        return labelRepository.findAll(pageable).stream()
                .map(labelMapper::toResponseLabelDto)
                .toList();
    }

    @Override
    public ResponseLabelDto updateById(Long labelId, CreateLabelDto createLabelDto) {
        final Label beforeLabel = labelRepository.findById(labelId).orElseThrow(() ->
                new EntityNotFoundException("Can't find label by id " + labelId));
        final String newName = updateValidName(createLabelDto.name(), labelId);
        final Set<Task> newTasks = updateValidTasks(createLabelDto.tasks(), labelId);
        beforeLabel.setTasks(newTasks);
        beforeLabel.setName(newName);
        beforeLabel.setColor(createLabelDto.color());
        return labelMapper.toResponseLabelDto(getSave(beforeLabel, newName));
    }

    @Override
    public void deleteById(Long labelId) {
        labelRepository.deleteById(labelId);
    }

    private Label getSave(Label label, String name) {
        try {
            return labelRepository.save(label);
        } catch (RuntimeException e) {
            throw new EntityNotFoundException("The label with name " + name
                    + " used before and then removed by using soft delete concept."
                    + "Please, rename label.");
        }
    }

    private String getValidName(String name) {
        labelRepository.findByName(name).ifPresent((foundLabel) -> {
            throw new EntityNotFoundException("The label with name " + name + " is exist");
        });
        return name;
    }

    private String updateValidName(String newName, Long labelIdBefore) {
        labelRepository.findByName(newName).ifPresent((foundLabel) -> {
            if (!foundLabel.getId().equals(labelIdBefore)) {
                throw new EntityNotFoundException("The label with name " + newName + " is exist");
            }
        });
        return newName;
    }

    private Set<Task> getValidTasks(Set<Long> tasks) {
        return tasks.stream()
                .map(taskId -> {
                    labelRepository.findByTasksId(taskId).ifPresent((foundLabel) -> {
                        throw new EntityNotFoundException("The task with id "
                                + taskId + " is added to another label");
                    });
                    return getTaskById(taskId);
                }).collect(Collectors.toSet());
    }

    private Set<Task> updateValidTasks(Set<Long> tasks, Long labelIdBefore) {
        return tasks.stream().map(taskId -> {
            labelRepository.findByTasksId(taskId).ifPresent((foundLabel) -> {
                if (!labelIdBefore.equals(foundLabel.getId())) {
                    throw new EntityNotFoundException("The task with id "
                            + taskId + " is added to another label");
                }
            });
                    return getTaskById(taskId);
                }
        ).collect(Collectors.toSet());
    }

    private Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("Can't find task by id " + taskId));
    }
}
