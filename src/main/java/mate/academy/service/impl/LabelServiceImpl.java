package mate.academy.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.label.CreateLabelDto;
import mate.academy.dto.label.ResponseLabelDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.LabelMapper;
import mate.academy.model.Label;
import mate.academy.model.Task;
import mate.academy.repository.label.LabelRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.LabelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ResponseLabelDto createLabel(CreateLabelDto createLabelDto) {
        final String name = createLabelDto.name();
        if (labelRepository.findByName(name).isPresent()) {
            throw new EntityNotFoundException("The label with name " + name + " is exist");
        }

        final Set<Task> tasks = createLabelDto.tasks().stream()
                .map(t -> {
                    if (labelRepository.findByTasksId(t).isPresent()) {
                        throw new EntityNotFoundException("The task with id "
                                + t + " is added to another label");
                    }
                    return taskRepository.findById(t).orElseThrow(() -> new EntityNotFoundException(
                            "Can't find task by id " + t));
                })
                .collect(Collectors.toSet());
        final Label label = new Label();
        label.setName(name);
        label.setColor(createLabelDto.color());
        label.setTasks(tasks);
        final ResponseLabelDto responseLabelDto =
                labelMapper.toResponseLabelDto(getSave(label, name));
        responseLabelDto.tasks().forEach(t -> t.getAssignee().setRoleDtos(getRoleDtos(t)));
        return responseLabelDto;
    }

    @Transactional
    @Override
    public List<ResponseLabelDto> findAll(Pageable pageable) {
        final Page<Label> labels = labelRepository.findAll(pageable);
        final List<ResponseLabelDto> labelDtos = labels.stream()
                .map(labelMapper::toResponseLabelDto)
                .toList();
        labelDtos.forEach(l -> l.tasks()
                .forEach(t -> t.getAssignee().setRoleDtos(getRoleDtos(t))));
        return labelDtos;
    }

    @Transactional
    @Override
    public ResponseLabelDto updateById(Long labelId, CreateLabelDto createLabelDto) {
        final String newName = createLabelDto.name();
        final Label beforeLabel = labelRepository.findById(labelId).orElseThrow(() ->
                new EntityNotFoundException("Can't find label by id " + labelId));
        if (labelRepository.findByName(newName).isPresent()
                && !newName.equals(beforeLabel.getName())) {
            throw new EntityNotFoundException("The label with name " + newName + " is exist");
        }
        final Set<Task> newTasks = createLabelDto.tasks().stream().map(taskId -> {
            if (labelRepository.findByTasksId(taskId).isPresent() && beforeLabel.getTasks().stream()
                    .filter(t -> t.getId().equals(taskId))
                    .toList().isEmpty()) {
                throw new EntityNotFoundException("The task with id " + taskId
                        + " is added to another label");
            }
            return taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException(
                    "Can't find task by id " + taskId));
        }).collect(Collectors.toSet());
        beforeLabel.setTasks(newTasks);
        beforeLabel.setName(newName);
        beforeLabel.setColor(createLabelDto.color());
        final ResponseLabelDto responseLabelDto = labelMapper
                .toResponseLabelDto(getSave(beforeLabel, newName));
        responseLabelDto.tasks().forEach(t -> t.getAssignee().setRoleDtos(getRoleDtos(t)));
        return responseLabelDto;
    }

    @Override
    public void deleteById(Long labelId) {
        labelRepository.deleteById(labelId);
    }

    private Set<String> getRoleDtos(ResponseTaskDto responseTaskDto) {
        return userRepository
                .findById(responseTaskDto.getAssignee().getId()).orElseThrow(
                        () -> new EntityNotFoundException("Can't find user by id "
                                + responseTaskDto.getAssignee().getId())).getRoles().stream()
                .map(r -> r.getName().toString())
                .collect(Collectors.toSet());
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
}
