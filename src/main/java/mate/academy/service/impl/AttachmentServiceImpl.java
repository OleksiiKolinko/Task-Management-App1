package mate.academy.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import mate.academy.dto.attachment.AllAttachmentsResponseDto;
import mate.academy.dto.attachment.AttachmentRequestDto;
import mate.academy.dto.attachment.AttachmentResponseDownloadDto;
import mate.academy.dto.attachment.AttachmentResponseDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.AttachmentMapper;
import mate.academy.mapper.TaskMapper;
import mate.academy.model.Attachment;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.repository.attachment.AttachmentRepository;
import mate.academy.repository.role.RoleRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.repository.task.TaskSpecificationBuilder;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.AttachmentService;
import mate.academy.service.DropboxService;
import mate.academy.service.EmailMessageUtil;
import mate.academy.service.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private static final Long ADMIN_ID = 1L;
    private static final Long MANAGER_ROLE_ID = 2L;
    private static final String SLASH = "/";
    private static final int ZERO = 0;
    private static final String NEW_ATTACHMENT = "New attachment";
    private static final String REMOVED_ATTACHMENT = "Removed attachment";
    private static final String REMOVED = "removed";
    private static final String ADDED = "added";
    private static final int ATTACHMENT_LIMIT = 50;

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final AttachmentMapper attachmentMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TaskSpecificationBuilder taskSpecificationBuilder;
    private final TaskMapper taskMapper;
    private final PaginationUtil paginationUtil;
    private final DropboxService dropboxService;
    private final EmailMessageUtil emailMessageUtil;

    @SneakyThrows
    @Override
    public AttachmentResponseDto uploadFile(AttachmentRequestDto requestDto, Long userId) {
        final User user = getUserById(userId);
        final Task task = getValidTask(requestDto.taskId(), user);
        final MultipartFile file = getValidFile(requestDto.file());
        final String fileName = getValidFilename(file.getOriginalFilename());
        final String dropboxFileId = dropboxService.upload(SLASH + fileName,
                file.getInputStream());
        final Attachment attachment = Attachment.builder().filename(fileName)
                .task(task).uploadDate(LocalDateTime.now()).dropboxFileId(dropboxFileId).build();
        final AttachmentResponseDto attachmentResponseDto = attachmentMapper
                .toAttachmentResponseDto(attachmentRepository.save(attachment));
        sendNotification(NEW_ATTACHMENT, ADDED, user, task, attachment);
        return attachmentResponseDto;
    }

    @Override
    public Page<AllAttachmentsResponseDto> getAllFiles(Pageable pageable,
                                                       TaskSearchParameters searchParameters,
                                                       Long userId) {
        final List<AllAttachmentsResponseDto> allAttachmentDtos =
                getAllAttachmentDtos(getTasksWithAttachments(userId, searchParameters));
        allAttachmentDtos.forEach(allAttachments -> allAttachments.attachments()
                .forEach(attachment -> attachment
                        .setDownload(dropboxService.download(attachment.getDropboxFileId()))));
        return paginationUtil.paginateList(pageable, allAttachmentDtos);
    }

    @Transactional
    @Override
    public void deleteById(Long attachmentId, Long userId) {
        final Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find attachment by id " + attachmentId));
        final Task task = attachment.getTask();
        final User user = getUserById(userId);
        getPermission(user, task);
        dropboxService.delete(attachment.getDropboxFileId());
        attachmentRepository.deleteById(attachmentId);
        sendNotification(REMOVED_ATTACHMENT, REMOVED, user, task, attachment);
    }

    private void sendNotification(String subject, String action, User user,
                                  Task task, Attachment attachment) {
        final User assignee = task.getAssignee();
        if (user.equals(assignee)) {
            emailMessageUtil.sendAddOrRemoveFile(subject, action,
                    userRepository.findByRolesId(MANAGER_ROLE_ID).stream()
                    .filter(user1 -> !user1.getId().equals(ADMIN_ID)
                            && !user1.equals(assignee))
                    .map(User::getEmail).collect(Collectors.toSet()), attachment, task);
        } else {
            emailMessageUtil.sendAddOrRemoveFile(subject, action,
                    Set.of(assignee.getEmail()), attachment, task);
        }
    }

    private void getPermission(User user, Task task) {
        if (!task.getAssignee().getId().equals(user.getId()) && !user.getRoles().contains(
                roleRepository.findById(MANAGER_ROLE_ID).orElseThrow(() ->
                        new EntityNotFoundException("Can't find role by id " + MANAGER_ROLE_ID)))) {
            throw new EntityNotFoundException(
                    "You can't manipulate with files of task if the task isn't yours "
                            + "and you don't have ROLE_MANAGER");
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id " + userId));
    }

    private List<Task> getTasksWithAttachments(Long userId, TaskSearchParameters searchParameters) {
        final User user = getUserById(userId);
        List<Task> tasksWithAttachments = taskRepository.findAll(
                        taskSpecificationBuilder.build(searchParameters)).stream()
                .filter(task -> !task.getAttachments().isEmpty())
                .toList();
        if (!user.getRoles().contains(roleRepository.findById(MANAGER_ROLE_ID).orElseThrow(() ->
                new EntityNotFoundException("Can't find role by id " + MANAGER_ROLE_ID)))) {
            tasksWithAttachments = tasksWithAttachments.stream()
                    .filter(task -> task.getAssignee().equals(user))
                    .toList();
        }
        if (tasksWithAttachments.isEmpty()) {
            throw new EntityNotFoundException("There are no attachments by this params for you");
        }
        return tasksWithAttachments;
    }

    private Task getValidTask(Long taskId, User user) {
        final Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new EntityNotFoundException("Can't find task by id " + taskId));
        getPermission(user, task);
        return task;
    }

    private MultipartFile getValidFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EntityNotFoundException("Please add the file");
        }
        return file;
    }

    private String getValidFilename(String originalFilename) {
        if (attachmentRepository.existsByFilename(originalFilename)) {
            throw new EntityNotFoundException(
                    "The file with name " + originalFilename
                            + " is exist on Dropbox. Please rename this file");
        }
        return originalFilename;
    }

    private List<AllAttachmentsResponseDto> getAllAttachmentDtos(List<Task> tasksWithAttachments) {
        final List<AllAttachmentsResponseDto> allAttachmentDtos = new ArrayList<>();
        for (final Task task : tasksWithAttachments) {
            final ResponseTaskDto taskDto =
                    taskMapper.toTaskResponseDto(task);
            final List<Attachment> attachments = task.getAttachments();
            List<AttachmentResponseDownloadDto> attachmentDownloadDtos = new ArrayList<>();
            if (attachments.size() > ATTACHMENT_LIMIT) {
                for (int l = ZERO; l < attachments.size(); l++) {
                    final Attachment attachment = attachments.get(l);
                    final AttachmentResponseDownloadDto attachmentDownloadDto = attachmentMapper
                            .toAttachmentDownloadDto(attachment);
                    attachmentDownloadDtos.add(attachmentDownloadDto);
                    if (attachmentDownloadDtos.size() == ATTACHMENT_LIMIT
                            || l == attachments.size() - 1) {
                        final AllAttachmentsResponseDto attachmentDtos =
                                new AllAttachmentsResponseDto(taskDto, attachmentDownloadDtos);
                        allAttachmentDtos.add(attachmentDtos);
                        attachmentDownloadDtos = new ArrayList<>();
                    }
                }
            } else {
                attachmentDownloadDtos =
                        attachments.stream()
                                .map(attachmentMapper::toAttachmentDownloadDto)
                                .toList();
                final AllAttachmentsResponseDto attachmentDtos =
                        new AllAttachmentsResponseDto(taskDto, attachmentDownloadDtos);
                allAttachmentDtos.add(attachmentDtos);
            }
        }
        return allAttachmentDtos;
    }
}
