package mate.academy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.config.SpringSecurityWebAuxTestConfig;
import mate.academy.dto.attachment.AttachmentResponseDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.service.DropboxService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = SpringSecurityWebAuxTestConfig.class)
public class AttachmentControllerTest {
    private static final String DROPBOX_FILE_ID = "dropboxFileId";
    private static final String DROPBOX_FILE_ID1 = "dropbox_file_id";
    private static final String DROPBOX_FILE_ID2 = "dropbox_file_id2";
    private static final int ONE = 1;
    private static final Long ONE_ID = 1L;
    private static final String FILE_NAME = "fileName.txt";
    private static final String TASK_NAME = "taskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final String MEDIUM = "MEDIUM";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String DUE_DATE = "2150-01-01";
    private static final String EMAIL = "email@example.com";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String START_DATE = "2100-01-01";
    private static final String END_DATE = "2200-01-01";
    private static final int ZERO = 0;
    private static final String LINK_DOWNLOAD = "linkDownload";
    private static final String LINK_DOWNLOAD2 = "linkDownload2";
    private static final String FILE = "file";
    private static final String INITIATED = "INITIATED";
    private static final String USER = "user";
    private static final Long TWO_ID = 2L;
    private static final Long THREE_ID = 3L;
    private static final int TWO = 2;
    private static final int SIXTEEN = 16;
    private static final byte[] CONTENT = "content".getBytes();
    private static final String TASK_ID_STRING = "1";
    private static final String TASK_ID_NAME = "taskId";
    private static final String FILENAME2 = "filename2";
    private static final String URL_POST_ATTACHMENTS = "/attachments";
    private static final String URL_GET_ALL_ATTACHMENTS = "/attachments?page=0&size=2&projectIds=1&"
            + "projectNames=projectName&taskIds=1&names=taskName&assigneeIds=2&assigneeNames=user";
    private static final String URL_DELETE_ATTACHMENT_3 = "/attachments/3";
    private static final String ADD_DATA_EXISTS = "database/attachment/add-data-exists.sql";
    private static final String REMOVE_AFTER_UPLOAD =
            "classpath:database/attachment/remove-after-upload-controller.sql";
    private static final String ADD_BEFORE_DELETE =
            "classpath:database/attachment/add-attachment-before-delete.sql";
    private static final String REMOVE_ALL_DATA = "database/attachment/remove-all-data.sql";
    private static final String USERNAME = "username";
    private static final String CONTENT_TASK_DTO_NAME = "$.content[0].taskDto.name";
    private static final String CONTENT_TASK_DTO_PROJECT_NAME = "$.content[0].taskDto.project.name";
    private static final String CONTENT_TASK_DTO_ASSIGNEE_USERNAME =
            "$.content[0].taskDto.assignee.username";
    private static final String CONTENT_TASK_DTO_ASSIGNEE_ROLE_DTOS =
            "$.content[0].taskDto.assignee.roleDtos";
    private static final String CONTENT_ATTACHMENTS_ID = "$.content[0].attachments[0].id";
    private static final String CONTENT_ATTACHMENTS_DOWNLOAD =
            "$.content[0].attachments[0].download";
    private static final String CONTENT_ATTACHMENTS2_FILENAME =
            "$.content[0].attachments[1].filename";
    private static final String CONTENT_ATTACHMENTS2_DOWNLOAD =
            "$.content[0].attachments[1].download";
    private static final String CONTENT_ATTACHMENT3 = "$.content[0].attachments[2]";
    private static final String SIZE = "$.size";
    private static final String TOTAL_ELEMENTS = "$.totalElements";
    private static final String PAGEABLE_PAGE_SIZE = "$.pageable.pageSize";
    private static final String PAGEABLE_PAGE_NUMBER = "$.pageable.pageNumber";
    private static MockMvc mockMvc;
    @MockBean
    private DropboxService dropboxService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext,
                          @Autowired DataSource dataSource) throws SQLException {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(ADD_DATA_EXISTS));
        }
    }

    @WithUserDetails(USERNAME)
    @SneakyThrows
    @Test
    @DisplayName("Verify uploadFile() method works")
    @Sql(scripts = REMOVE_AFTER_UPLOAD, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void uploadFile_Valid_ReturnAttachmentResponseDto() {
        final MockMultipartFile file = new MockMultipartFile(FILE, FILE_NAME,
                MediaType.TEXT_PLAIN_VALUE, CONTENT);
        final AttachmentResponseDto expect = new AttachmentResponseDto(THREE_ID,
                getResponseTaskDto(), DROPBOX_FILE_ID, FILE_NAME, LocalDateTime.now().toString());
        when(dropboxService.upload(anyString(),any(InputStream.class))).thenReturn(DROPBOX_FILE_ID);
        final MvcResult result = mockMvc.perform(multipart(URL_POST_ATTACHMENTS)
                .file(file)
                .param(TASK_ID_NAME, TASK_ID_STRING)).andExpect(status().isOk()).andReturn();
        final AttachmentResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), AttachmentResponseDto.class);
        assertThat(actual.task()).isEqualTo(expect.task());
        assertThat(actual.dropboxFileId()).isEqualTo(expect.dropboxFileId());
        assertThat(actual.filename()).isEqualTo(expect.filename());
        assertThat(actual.uploadDate().substring(ZERO, SIXTEEN))
                .isEqualTo(expect.uploadDate().substring(ZERO, SIXTEEN));
    }

    @WithUserDetails(USERNAME)
    @SneakyThrows
    @Test
    @DisplayName("Verify getAllFiles() method works")
    public void getAllFiles_ValidFile_ReturnPageAllAttachmentsResponseDto() {
        when(dropboxService.download(DROPBOX_FILE_ID1)).thenReturn(LINK_DOWNLOAD);
        when(dropboxService.download(DROPBOX_FILE_ID2)).thenReturn(LINK_DOWNLOAD2);
        mockMvc.perform(get(URL_GET_ALL_ATTACHMENTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CONTENT_TASK_DTO_NAME).value(TASK_NAME))
                .andExpect(jsonPath(CONTENT_TASK_DTO_PROJECT_NAME).value(PROJECT_NAME))
                .andExpect(jsonPath(CONTENT_TASK_DTO_ASSIGNEE_USERNAME).value(USER))
                .andExpect(jsonPath(CONTENT_TASK_DTO_ASSIGNEE_ROLE_DTOS).value(ROLE_USER))
                .andExpect(jsonPath(CONTENT_ATTACHMENTS_ID).value(ONE))
                .andExpect(jsonPath(CONTENT_ATTACHMENTS_DOWNLOAD).value(LINK_DOWNLOAD))
                .andExpect(jsonPath(CONTENT_ATTACHMENTS2_FILENAME).value(FILENAME2))
                .andExpect(jsonPath(CONTENT_ATTACHMENTS2_DOWNLOAD).value(LINK_DOWNLOAD2))
                .andExpect(jsonPath(SIZE).value(TWO))
                .andExpect(jsonPath(TOTAL_ELEMENTS).value(ONE))
                .andExpect(jsonPath(PAGEABLE_PAGE_SIZE).value(TWO))
                .andExpect(jsonPath(PAGEABLE_PAGE_NUMBER).value(ZERO));
    }

    @SneakyThrows
    @WithUserDetails(USERNAME)
    @Test
    @DisplayName("Verify deleteById() method works")
    @Sql(scripts = ADD_BEFORE_DELETE, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void deleteById_Valid_Deleted() {
        mockMvc.perform(get(URL_GET_ALL_ATTACHMENTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(CONTENT_ATTACHMENT3).exists());
        mockMvc.perform(delete(URL_DELETE_ATTACHMENT_3)).andExpect(status().isOk());
        mockMvc.perform(get(URL_GET_ALL_ATTACHMENTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(CONTENT_ATTACHMENT3).doesNotExist());
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(REMOVE_ALL_DATA));
        }
    }

    private ResponseTaskDto getResponseTaskDto() {
        return new ResponseTaskDto(ONE_ID, TASK_NAME,TASK_DESCRIPTION, MEDIUM, IN_PROGRESS,
                DUE_DATE, new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, INITIATED), new UserResponseDtoWithRole(TWO_ID, USER,
                EMAIL, FIRST_NAME, LAST_NAME, Set.of(ROLE_USER)));
    }
}
