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
import mate.academy.service.EmailService;
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
    private static final String TOP_BODY_TEXT_REMOVED =
            "This file was removed" + System.lineSeparator();
    private static final String TOP_BODY_TEXT_ADD = "This file was added" + System.lineSeparator();
    private static final String ATTACHMENT_ID = "attachment id ";
    private static final String FILENAME = " and filename ";
    private static final String REFERS_TO_TASK = ", refers to the task ";
    private static final String WITH_TASK_ID = " with task id ";
    private static final String UPLOADED = ". This file uploaded ";
    private static final String FILE_ID = " and received dropbox file id ";
    private static final int ATTACHMENT_LIMIT = 50;

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final AttachmentMapper attachmentMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TaskSpecificationBuilder taskSpecificationBuilder;
    private final EmailService emailService;
    private final TaskMapper taskMapper;
    private final PaginationUtil paginationUtil;
    private final DropboxService dropboxService;

    @SneakyThrows
    @Override
    public AttachmentResponseDto uploadFile(AttachmentRequestDto requestDto, Long userId) {
        final Long taskId = requestDto.taskId();
        final Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new EntityNotFoundException("Can't find task by id " + taskId));
        final MultipartFile file = requestDto.file();
        if (file.isEmpty()) {
            throw new EntityNotFoundException("Please add the file");
        }
        getPermission(userId, task);
        final String fileName = file.getOriginalFilename();
        if (attachmentRepository.existsByFilename(fileName)) {
            throw new EntityNotFoundException(
                "The file with name " + fileName + " is exist on Dropbox. Please rename this file");
        }
        final String dropboxFileId = dropboxService.upload(SLASH + fileName,
                requestDto.file().getInputStream());
        final Attachment attachment = new Attachment();
        attachment.setFilename(fileName);
        attachment.setTask(task);
        attachment.setUploadDate(LocalDateTime.now());
        attachment.setDropboxFileId(dropboxFileId);
        attachmentRepository.save(attachment);
        final AttachmentResponseDto attachmentResponseDto = attachmentMapper
                .toAttachmentResponseDto(attachment);
        attachmentResponseDto.task()
                .getAssignee()
                .setRoleDtos(
                attachment.getTask().getAssignee().getRoles().stream()
                .map(r -> r.getName().toString())
                .collect(Collectors.toSet()));
        sendNotification(userId, task, NEW_ATTACHMENT,
                getBodyText(TOP_BODY_TEXT_ADD, task, attachment));
        return attachmentResponseDto;
    }

    @Transactional
    @Override
    public Page<AllAttachmentsResponseDto> getAllFiles(Pageable pageable,
                                                       TaskSearchParameters searchParameters,
                                                       Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Can't find user by id " + userId));
        List<Task> tasksWithAttachments = taskRepository.findAll(
                taskSpecificationBuilder.build(searchParameters)).stream()
                .filter(t -> !attachmentRepository.findAllByTaskId(t.getId()).isEmpty())
                .toList();
        if (!user.getRoles().contains(roleRepository.findById(MANAGER_ROLE_ID).orElseThrow(() ->
                new EntityNotFoundException("Can't find role by id " + MANAGER_ROLE_ID)))) {
            tasksWithAttachments = tasksWithAttachments.stream()
                    .filter(t -> t.getAssignee().equals(user))
                    .toList();
        }
        if (tasksWithAttachments.isEmpty()) {
            throw new EntityNotFoundException("There are no attachments by this params for you");
        }
        final List<AllAttachmentsResponseDto> allAttachmentDtos = new ArrayList<>();
        for (final Task task : tasksWithAttachments) {
            final ResponseTaskDto taskDto =
                    taskMapper.toTaskResponseDto(task);
            taskDto.getAssignee().setRoleDtos(task.getAssignee().getRoles()
                    .stream().map(r -> r.getName().toString()).collect(Collectors.toSet()));
            final List<Attachment> attachments = attachmentRepository
                    .findAllByTaskId(task.getId());
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
        allAttachmentDtos.forEach(al -> al.attachments()
                .forEach(a -> a.setDownload(dropboxService.download(a.getDropboxFileId()))));
        return paginationUtil.paginateList(pageable, allAttachmentDtos);
    }

    @Transactional
    @Override
    public void deleteById(Long attachmentId, Long userId) {
        final Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find attachment by id " + attachmentId));
        Task attachmentTask = attachment.getTask();
        getPermission(userId, attachmentTask);
        dropboxService.delete(attachment.getDropboxFileId());
        attachmentRepository.deleteById(attachmentId);
        sendNotification(userId, attachmentTask, REMOVED_ATTACHMENT,
                getBodyText(TOP_BODY_TEXT_REMOVED, attachmentTask, attachment));
    }

    private void getPermission(Long userId, Task task) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id " + userId));
        if (!task.getAssignee().equals(user) && !user.getRoles().contains(roleRepository
                .findById(MANAGER_ROLE_ID).orElseThrow(() -> new EntityNotFoundException(
                        "Can't find role by id " + MANAGER_ROLE_ID)))) {
            throw new EntityNotFoundException(
                    "You can't manipulate with files of task if the task isn't yours.");
        }
    }

    private void sendNotification(Long userId, Task task, String subject, String bodyText) {
        final User assignee = task.getAssignee();
        if (userId.equals(assignee.getId())) {
            final Set<User> managers = userRepository.findByRolesId(MANAGER_ROLE_ID);
            final List<String> emails = managers.stream()
                    .filter(u -> !u.getId().equals(ADMIN_ID) && !u.getId().equals(userId))
                    .map(User::getEmail)
                    .toList();
            for (String email : emails) {
                emailService.sendEmail(email, subject, bodyText);
            }
        } else {
            emailService.sendEmail(assignee.getEmail(), subject, bodyText);
        }
    }

    private String getBodyText(String topBodyText, Task task, Attachment attachment) {
        return new StringBuilder(topBodyText).append(ATTACHMENT_ID).append(attachment.getId())
                .append(FILENAME).append(attachment.getFilename()).append(REFERS_TO_TASK)
                .append(task.getName()).append(WITH_TASK_ID).append(task.getId()).append(UPLOADED)
                .append(attachment.getUploadDate()).append(FILE_ID)
                .append(attachment.getDropboxFileId()).toString();
    }
}
