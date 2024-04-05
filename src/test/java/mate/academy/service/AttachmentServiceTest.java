package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.attachment.AllAttachmentsResponseDto;
import mate.academy.dto.attachment.AttachmentRequestDto;
import mate.academy.dto.attachment.AttachmentResponseDownloadDto;
import mate.academy.dto.attachment.AttachmentResponseDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskSearchParameters;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.mapper.AttachmentMapper;
import mate.academy.mapper.TaskMapper;
import mate.academy.model.Attachment;
import mate.academy.model.Project;
import mate.academy.model.Role;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.repository.attachment.AttachmentRepository;
import mate.academy.repository.role.RoleRepository;
import mate.academy.repository.task.TaskRepository;
import mate.academy.repository.task.TaskSpecificationBuilder;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.impl.AttachmentServiceImpl;
import mate.academy.service.impl.PaginationUtilImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceTest {
    private static final String DROPBOX_FILE_ID = "dropboxFileId";
    private static final int ONE = 1;
    private static final Long ONE_ID = 1L;
    private static final String FILE_NAME = "fileName";
    private static final String INPUT_STREAM = "InputStream";
    private static final String UPLOAD_DATE = "2145-01-01-00-00-00.000";
    private static final String TASK_NAME = "taskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final String MEDIUM = "MEDIUM";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String DUE_DATE = "2150-01-01";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email@example.com";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String START_DATE = "2100-01-01";
    private static final String END_DATE = "2200-01-01";
    private static final int UPLOAD_YEAR = 2145;
    private static final int ZERO = 0;
    private static final int DUE_YEAR = 2150;
    private static final String PASSWORD = "password";
    private static final int START_YEAR = 2100;
    private static final int END_YEAR = 2200;
    private static final PageRequest PAGEABLE = PageRequest.of(0, 20);
    private static final String LINK_DOWNLOAD = "linkDownload";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final Long TWO_ID = 2L;
    private static final Role.RoleName ROLE_NAME_MANAGER = Role.RoleName.ROLE_MANAGER;
    private static final Role.RoleName ROLE_NAME_USER = Role.RoleName.ROLE_USER;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AttachmentMapper attachmentMapper;
    @Mock
    private DropboxService dropboxService;
    @Mock
    private TaskSpecificationBuilder taskSpecificationBuilder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private PaginationUtil paginationUtil;
    @Mock
    private EmailMessageUtil emailMessageUtil;
    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @Test
    @DisplayName("Verify uploadFile() method works")
    public void uploadFile_ValidFile_ReturnAttachmentResponseDto() {
        final AttachmentResponseDto expect = new AttachmentResponseDto(ONE_ID, getResponseTaskDto(),
                DROPBOX_FILE_ID, FILE_NAME, UPLOAD_DATE);
        when(dropboxService.upload(ArgumentMatchers.anyString(), any(InputStream.class)))
                .thenReturn(DROPBOX_FILE_ID);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getAssignee()));
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(getTask()));
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(getAttachment());
        when(attachmentMapper.toAttachmentResponseDto(any(Attachment.class))).thenReturn(expect);
        final AttachmentResponseDto actual = attachmentService
                .uploadFile(new AttachmentRequestDto(ONE_ID, new MockMultipartFile(FILE_NAME,
                        INPUT_STREAM.getBytes())), getAssignee().getId());
        assertThat(actual).isEqualTo(expect);
        verify(dropboxService, times(ONE))
                .upload(ArgumentMatchers.anyString(), any(InputStream.class));
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(taskRepository, times(ONE)).findById(anyLong());
        verify(attachmentRepository, times(ONE)).save(any(Attachment.class));
        verify(attachmentMapper, times(ONE)).toAttachmentResponseDto(any(Attachment.class));
        verify(userRepository, times(ONE)).findByRolesId(anyLong());
        verify(attachmentRepository, times(ONE))
                .existsByFilename(anyString());
        verifyNoMoreInteractions(dropboxService, userRepository,
                taskRepository, attachmentRepository, attachmentMapper);
    }

    @Test
    @DisplayName("Verify getAllFiles() method works")
    public void getAllFiles_ValidFile_ReturnAllAttachmentsResponseDto() {
        final Page<AllAttachmentsResponseDto> expect = getPageAllAttachmentsResponseDto();
        TaskSearchParameters taskSearchParameters = getTaskSearchParameters();
        final Task task = getTask();
        task.getAttachments().add(getAttachment());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getAssignee()));
        when(taskRepository.findAll(taskSpecificationBuilder
                .build(any(TaskSearchParameters.class)))).thenReturn(List.of(task));
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(getRoleManager()));
        when(taskMapper.toTaskResponseDto(any(Task.class))).thenReturn(getResponseTaskDto());
        when(attachmentMapper.toAttachmentDownloadDto(any(Attachment.class)))
                .thenReturn(getListAttachmentResponseDownloadDto().get(ZERO));
        when(dropboxService.download(anyString())).thenReturn(LINK_DOWNLOAD);
        when(paginationUtil.paginateList(PAGEABLE, getListAllAttachmentsResponseDto()))
                .thenReturn(getPageAllAttachmentsResponseDto());
        final Page<AllAttachmentsResponseDto> actual =
                attachmentService.getAllFiles(PAGEABLE, taskSearchParameters, ONE_ID);
        assertThat(actual).isEqualTo(expect);
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(taskRepository, times(ONE))
                .findAll(taskSpecificationBuilder.build(any(TaskSearchParameters.class)));
        verify(roleRepository, times(ONE)).findById(anyLong());
        verify(taskMapper, times(ONE)).toTaskResponseDto(any(Task.class));
        verify(attachmentMapper, times(ONE)).toAttachmentDownloadDto(any(Attachment.class));
        verify(dropboxService, times(ONE)).download(anyString());
        verify(paginationUtil, times(ONE)).paginateList(PAGEABLE,
                getListAllAttachmentsResponseDto());
        verifyNoMoreInteractions(userRepository, taskRepository, roleRepository,
                attachmentRepository, taskMapper, attachmentMapper, dropboxService, paginationUtil);
    }

    @Test
    @DisplayName("Verify deleteById() method works")
    public void deleteById_ValidFile_deleted() {
        when(attachmentRepository.findById(anyLong())).thenReturn(Optional.of(getAttachment()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(getAssignee()));
        attachmentService.deleteById(ONE_ID, ONE_ID);
        verify(attachmentRepository, times(ONE)).deleteById(ONE_ID);
        verify(userRepository, times(ONE)).findById(anyLong());
        verify(attachmentRepository, times(ONE)).findById(anyLong());
        verify(userRepository, times(ONE)).findByRolesId(anyLong());
        verifyNoMoreInteractions(attachmentRepository, userRepository);
    }

    private TaskSearchParameters getTaskSearchParameters() {
        final Long[] taskIds = new Long[ONE];
        taskIds[ZERO] = ONE_ID;
        final String[] taskName = new String[ONE];
        taskName[ZERO] = TASK_NAME;
        final Long[] projectIds = new Long[ONE];
        projectIds[ZERO] = ONE_ID;
        final String[] projectName = new String[ONE];
        projectName[ZERO] = PROJECT_NAME;
        final Long[] assigneeIds = new Long[ONE];
        assigneeIds[ZERO] = ONE_ID;
        final String[] assigneeNames = new String[ONE];
        assigneeNames[ZERO] = USERNAME;
        return new TaskSearchParameters(taskIds, taskName, projectIds,
                projectName, assigneeIds, assigneeNames);
    }

    private Page<AllAttachmentsResponseDto> getPageAllAttachmentsResponseDto() {
        return new PaginationUtilImpl().paginateList(PAGEABLE, getListAllAttachmentsResponseDto());
    }

    private List<AllAttachmentsResponseDto> getListAllAttachmentsResponseDto() {
        return List.of(new AllAttachmentsResponseDto(getResponseTaskDto(),
                getListAttachmentResponseDownloadDto()));
    }

    private List<AttachmentResponseDownloadDto> getListAttachmentResponseDownloadDto() {
        return List.of(new AttachmentResponseDownloadDto(ONE_ID, FILE_NAME, UPLOAD_DATE,
                DROPBOX_FILE_ID, LINK_DOWNLOAD));
    }

    private ResponseTaskDto getResponseTaskDto() {
        return new ResponseTaskDto(ONE_ID, TASK_NAME,TASK_DESCRIPTION, MEDIUM, IN_PROGRESS,
                DUE_DATE, new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS), new UserResponseDtoWithRole(ONE_ID, USERNAME,
                EMAIL, FIRST_NAME, LAST_NAME, Set.of(ROLE_USER, ROLE_MANAGER)));
    }

    private Attachment getAttachment() {
        return Attachment.builder().id(ONE_ID).task(getTask()).filename(FILE_NAME)
                .dropboxFileId(DROPBOX_FILE_ID).uploadDate(LocalDateTime.of(
                        UPLOAD_YEAR, Month.JANUARY,ONE,ZERO,ZERO,ZERO,ZERO)).build();
    }

    private Task getTask() {
        return Task.builder().id(ONE_ID).name(TASK_NAME).description(TASK_DESCRIPTION).priority(
                Task.Priority.MEDIUM).status(Task.Status.IN_PROGRESS).dueDate(LocalDate.of(
                        DUE_YEAR,ONE,ONE)).project(Project.builder().id(ONE_ID).name(PROJECT_NAME)
                        .description(PROJECT_DESCRIPTION)
                .startDate(LocalDate.of(START_YEAR,ONE,ONE))
                        .endDate(LocalDate.of(END_YEAR,ONE, ONE)).status(Project.Status.IN_PROGRESS)
                        .build()).assignee(getAssignee()).attachments(new ArrayList<>()).build();
    }

    private User getAssignee() {
        return User.builder().id(ONE_ID).username(USERNAME).password(PASSWORD).email(EMAIL)
                .firstName(FIRST_NAME).lastName(LAST_NAME).roles(getRoles()).build();
    }

    private Set<Role> getRoles() {
        final Role roleUser = new Role();
        roleUser.setId(ONE_ID);
        roleUser.setName(ROLE_NAME_USER);
        return Set.of(roleUser, getRoleManager());
    }

    private Role getRoleManager() {
        final Role roleManager = new Role();
        roleManager.setId(TWO_ID);
        roleManager.setName(ROLE_NAME_MANAGER);
        return roleManager;
    }
}
