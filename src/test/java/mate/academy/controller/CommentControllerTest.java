package mate.academy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import mate.academy.config.SpringSecurityWebAuxTestConfig;
import mate.academy.dto.comment.CommentDto;
import mate.academy.dto.comment.CommentRequestDto;
import mate.academy.dto.comment.CommentResponseDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.user.UserResponseDtoWithRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = SpringSecurityWebAuxTestConfig.class)
public class CommentControllerTest {
    private static final int ONE = 1;
    private static final Long ONE_ID = 1L;
    private static final String TASK_NAME = "taskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final String MEDIUM = "MEDIUM";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String DUE_DATE = "2150-01-01";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email@example.com";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String START_DATE = "2100-01-01";
    private static final String END_DATE = "2200-01-01";
    private static final int ZERO = 0;
    private static final int SIXTEEN = 16;
    private static final Long TWO_ID = 2L;
    private static final Long THREE_ID = 3L;
    private static final int THREE = 3;
    private static final String TEXT_1 = "text1";
    private static final String INITIATED = "INITIATED";
    private static final String URL_COMMENTS = "/comments";
    private static final String URL_GET_ALL_COMMENTS = "/comments?page=0&size=3&projectIds=1&projec"
            + "tNames=projectName&taskIds=1&names=taskName&assigneeIds=2&assigneeNames=username";
    private static final String ADD_COMMENTS = "classpath:database/comment/add-data-comments.sql";
    private static final String REMOVE_COMMENTS =
            "classpath:database/comment/remove-all-comments.sql";
    private static final String CONTENT_TASK_DTO_NAME = "$.content[0].taskDto.name";
    private static final String CONTENT_TASK_DTO_PROJECT_NAME = "$.content[0].taskDto.project.name";
    private static final String CONTENT_TASK_DTO_ASSIGNEE_USERNAME =
            "$.content[0].taskDto.assignee.username";
    private static final String CONTENT_TASK_DTO_ASSIGNEE_ROLE_DTOS =
            "$.content[0].taskDto.assignee.roleDtos";
    private static final String CONTENT_TASK_DTO_ID = "$.content[0].taskDto.id";
    private static final String CONTENT_COMMENT_DTO_2_ID = "$.content[0].commentDtos[2].id";
    private static final String CONTENT_COMMENT_DTO_1_ID = "$.content[0].commentDtos[1].id";
    private static final String CONTENT_COMMENT_DTO_0_ID = "$.content[0].commentDtos[0].id";
    private static final String CONTENT_COMMENT_DTO_2_USERNAME =
            "$.content[0].commentDtos[2].username";
    private static final String CONTENT_COMMENT_DTO_1_USERNAME =
            "$.content[0].commentDtos[1].username";
    private static final String CONTENT_COMMENT_DTO_0_USERNAME =
            "$.content[0].commentDtos[0].username";
    private static final String CONTENT_COMMENT_DTO_2_TEXT = "$.content[0].commentDtos[2].text";
    private static final String CONTENT_COMMENT_DTO_1_TEXT = "$.content[0].commentDtos[1].text";
    private static final String CONTENT_COMMENT_DTO_0_TEXT = "$.content[0].commentDtos[0].text";
    private static final String CONTENT_COMMENT_DTO2 = "$.content[0].commentDtos[2]";
    private static final String TEXT = "text";
    private static final String TEXT_2 = "text2";
    private static final String SIZE = "$.size";
    private static final String TOTAL_ELEMENTS = "$.totalElements";
    private static final String PAGEABLE_PAGE_SIZE = "$.pageable.pageSize";
    private static final String PAGEABLE_PAGE_NUMBER = "$.pageable.pageNumber";
    private static final String URL_DELETE_COMMENTS_3 = "/comments/3";
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithUserDetails(USERNAME)
    @SneakyThrows
    @Test
    @DisplayName("Verify addComment() method works")
    @Sql(scripts = ADD_COMMENTS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_COMMENTS, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addComment_Valid_ReturnCommentResponseDto() {
        final CommentResponseDto expect = new CommentResponseDto(getResponseTaskDto(), List.of(
                new CommentDto(ONE_ID, LocalDateTime.now().toString(), USERNAME, TEXT_1)));
        final String jsonRequest = objectMapper
                .writeValueAsString(new CommentRequestDto(ONE_ID, TEXT_1));
        final MvcResult result = mockMvc.perform(post(URL_COMMENTS).content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final CommentResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CommentResponseDto.class);
        assertThat(actual.commentDtos().get(THREE).timestamp().substring(ZERO, SIXTEEN))
                .isEqualTo(expect.commentDtos().get(ZERO).timestamp().substring(ZERO, SIXTEEN));
        assertThat(actual.commentDtos().get(THREE).username())
                .isEqualTo(expect.commentDtos().get(ZERO).username());
        assertThat(actual.commentDtos().get(THREE).text())
                .isEqualTo(expect.commentDtos().get(ZERO).text());
        assertThat(actual.taskDto()).isEqualTo(expect.taskDto());
    }

    @WithMockUser
    @SneakyThrows
    @Test
    @DisplayName("Verify getAllComments() method works")
    @Sql(scripts = ADD_COMMENTS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_COMMENTS, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getAllComments_Valid_ReturnPageCommentResponseDto() {
        mockMvc.perform(get(URL_GET_ALL_COMMENTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CONTENT_TASK_DTO_NAME).value(TASK_NAME))
                .andExpect(jsonPath(CONTENT_TASK_DTO_ID).value(ONE_ID))
                .andExpect(jsonPath(CONTENT_TASK_DTO_PROJECT_NAME).value(PROJECT_NAME))
                .andExpect(jsonPath(CONTENT_TASK_DTO_ASSIGNEE_USERNAME).value(USERNAME))
                .andExpect(jsonPath(CONTENT_TASK_DTO_ASSIGNEE_ROLE_DTOS).value(ROLE_USER))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_2_ID).value(ONE_ID))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_1_ID).value(TWO_ID))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_0_ID).value(THREE_ID))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_2_USERNAME).value(USERNAME))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_1_USERNAME).value(USERNAME))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_0_USERNAME).value(USERNAME))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_2_TEXT).value(TEXT))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_1_TEXT).value(TEXT_1))
                .andExpect(jsonPath(CONTENT_COMMENT_DTO_0_TEXT).value(TEXT_2))
                .andExpect(jsonPath(SIZE).value(THREE))
                .andExpect(jsonPath(TOTAL_ELEMENTS).value(ONE))
                .andExpect(jsonPath(PAGEABLE_PAGE_SIZE).value(THREE))
                .andExpect(jsonPath(PAGEABLE_PAGE_NUMBER).value(ZERO));
    }

    @WithUserDetails(USERNAME)
    @SneakyThrows
    @Test
    @DisplayName("Verify deleteById() method works")
    @Sql(scripts = ADD_COMMENTS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_COMMENTS, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteById_Valid_Deleted() {
        mockMvc.perform(get(URL_GET_ALL_COMMENTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath(CONTENT_COMMENT_DTO2).exists());
        mockMvc.perform(delete(URL_DELETE_COMMENTS_3)).andExpect(status().isOk());
        mockMvc.perform(get(URL_GET_ALL_COMMENTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath(CONTENT_COMMENT_DTO2)
                        .doesNotExist());
    }

    private ResponseTaskDto getResponseTaskDto() {
        return new ResponseTaskDto(ONE_ID, TASK_NAME,TASK_DESCRIPTION, MEDIUM, IN_PROGRESS,
                DUE_DATE, new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, INITIATED), new UserResponseDtoWithRole(TWO_ID, USERNAME,
                EMAIL, FIRST_NAME, LAST_NAME, Set.of(ROLE_USER)));
    }
}
