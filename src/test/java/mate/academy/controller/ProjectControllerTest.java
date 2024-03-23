package mate.academy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import mate.academy.config.SpringSecurityWebAuxTestConfig;
import mate.academy.dto.project.CreateProjectDto;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.project.UpdateProjectDto;
import mate.academy.service.DropboxService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = SpringSecurityWebAuxTestConfig.class)
public class ProjectControllerTest {
    private static final int ONE = 1;
    private static final Long ONE_ID = 1L;
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String PROJECT_NAME = "projectName";
    private static final String UPDATE_PROJECT_NAME = "updateProjectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String START_DATE = "2100-01-01";
    private static final String END_DATE = "2200-01-01";
    private static final String MANAGER = "MANAGER";
    private static final String INITIATED = "INITIATED";
    private static final String URL_PROJECTS = "/projects";
    private static final String REMOVE_ALL_PROJECT =
            "classpath:database/project/remove-all-project.sql";
    private static final String ADD_DATA_PROJECT =
            "classpath:database/project/add-data-project.sql";
    private static final String ADD_DATA_BEFORE_REMOVE =
            "classpath:database/project/add-data-before-remove-project.sql";
    private static final String REMOVE_AFTER_DELETE =
            "classpath:database/project/remove-after-delete-project.sql";
    private static final String URL_PROJECTS_1 = "/projects/1";
    private static final String SIZE = "$.*";
    private static final String USER = "USER";
    private static final int ZERO = 0;
    private static final String ID = "id";
    private static MockMvc mockMvc;
    @MockBean
    private DropboxService dropboxService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(roles = MANAGER)
    @SneakyThrows
    @Test
    @DisplayName("Verify createProject() method works")
    @Sql(scripts = REMOVE_ALL_PROJECT, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createProject_Valid_ReturnResponseProjectDto() {
        final MvcResult result = mockMvc.perform(post(URL_PROJECTS).content(objectMapper
                .writeValueAsString(new CreateProjectDto(PROJECT_NAME, PROJECT_DESCRIPTION,
                        START_DATE, END_DATE))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        final ResponseProjectDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), ResponseProjectDto.class);
        assertThat(actual).usingRecursiveComparison().ignoringFields(ID)
                .isEqualTo(getResponseProjectDto());
    }

    @WithMockUser
    @SneakyThrows
    @Test
    @DisplayName("Verify getAllProjects() method works")
    @Sql(scripts = ADD_DATA_PROJECT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_PROJECT, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getAllProjects_Valid_ListResponseProjectDto() {
        final MvcResult result = mockMvc.perform(get(URL_PROJECTS).contentType(
                MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final ResponseProjectDto[] actual = objectMapper.readValue(result.getResponse()
                        .getContentAsString(), ResponseProjectDto[].class);
        assertThat(Arrays.stream(actual).toList()).isEqualTo(List.of(getResponseProjectDto()));
    }

    @WithMockUser
    @SneakyThrows
    @Test
    @DisplayName("Verify getProject() method works")
    @Sql(scripts = ADD_DATA_PROJECT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_PROJECT, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getProject_Valid_ReturnResponseProjectDto() {
        final MvcResult result = mockMvc.perform(get(URL_PROJECTS_1).contentType(
                MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final ResponseProjectDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ResponseProjectDto.class);
        assertThat(actual).isEqualTo(getResponseProjectDto());
    }

    @WithMockUser(roles = MANAGER)
    @SneakyThrows
    @Test
    @DisplayName("Verify updateProject() method works")
    @Sql(scripts = ADD_DATA_PROJECT, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_PROJECT, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateProject_Valid_ReturnResponseProjectDto() {
        final MvcResult result = mockMvc.perform(put(URL_PROJECTS_1).content(objectMapper
                        .writeValueAsString(new UpdateProjectDto(UPDATE_PROJECT_NAME,
                                PROJECT_DESCRIPTION, START_DATE, END_DATE, IN_PROGRESS)))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final ResponseProjectDto actual = objectMapper.readValue(result.getResponse()
                        .getContentAsString(), ResponseProjectDto.class);
        assertThat(actual).isEqualTo(new ResponseProjectDto(ONE_ID, UPDATE_PROJECT_NAME,
                PROJECT_DESCRIPTION, START_DATE, END_DATE, IN_PROGRESS));
    }

    @WithMockUser(roles = {MANAGER, USER})
    @SneakyThrows
    @Test
    @DisplayName("Verify deleteProject() method works")
    @Sql(scripts = ADD_DATA_BEFORE_REMOVE, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_AFTER_DELETE, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteProject_Valid_Deleted() {
        mockMvc.perform(get(URL_PROJECTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath(SIZE, hasSize(ONE)));
        mockMvc.perform(delete(URL_PROJECTS_1).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get(URL_PROJECTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath(SIZE, hasSize(ZERO)));
    }

    private ResponseProjectDto getResponseProjectDto() {
        return new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, INITIATED);
    }
}
