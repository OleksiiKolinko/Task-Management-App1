package mate.academy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import mate.academy.config.SpringSecurityWebAuxTestConfig;
import mate.academy.dto.project.ResponseProjectDto;
import mate.academy.dto.task.ResponseTaskDto;
import mate.academy.dto.task.TaskDtoCreate;
import mate.academy.dto.user.UserResponseDtoWithRole;
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
public class TaskControllerTest {
    private static final Long ONE_ID = 1L;
    private static final String TASK_NAME = "taskName";
    private static final String UPDATE_TASK_NAME = "updateTaskName";
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
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final Long TWO_ID = 2L;
    private static final String MANAGER = "MANAGER";
    private static final String URL_TASKS = "/tasks";
    private static final String URL_TASK_1 = "/tasks/1";
    private static final String URL_FIND_ALL = "/tasks?page=0&size=2&projectIds=1&projectNames"
            + "=projectName&taskIds=1&names=taskName&assigneeIds=2&assigneeNames=username";
    private static final String ID = "id";
    private static final String ADD_DATA_CREATE_TASK =
            "classpath:database/task/add-data-create-task-controller.sql";
    private static final String REMOVE_ALL_TASK = "classpath:database/task/remove-all-tasks.sql";
    private static final String ADD_DATA_TASKS = "classpath:database/task/add-data-tasks.sql";
    private static final String ADD_DATA_BEFORE_DELETE_TASK =
            "classpath:database/task/add-data-before-delete-task-controller.sql";
    private static final String USER = "USER";
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
    @DisplayName("Verify createTask() method works")
    @Sql(scripts = ADD_DATA_CREATE_TASK, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_TASK, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createTask_Valid_ReturnResponseTaskDto() {
        final MvcResult result = mockMvc.perform(post(URL_TASKS).content(objectMapper
                        .writeValueAsString(new TaskDtoCreate(TASK_NAME, TASK_DESCRIPTION, MEDIUM,
                                IN_PROGRESS, DUE_DATE, ONE_ID, TWO_ID))).contentType(
                                        MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        final ResponseTaskDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ResponseTaskDto.class);
        assertThat(actual).usingRecursiveComparison().ignoringFields(ID)
                .isEqualTo(getResponseTaskDto());
    }

    @WithMockUser
    @SneakyThrows
    @Test
    @DisplayName("Verify getAllTasks() method works")
    @Sql(scripts = ADD_DATA_TASKS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_TASK, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getAllTasks_Valid_ReturnListResponseTaskDto() {
        final MvcResult result = mockMvc.perform(get(URL_FIND_ALL).contentType(
                MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final ResponseTaskDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ResponseTaskDto[].class);
        assertThat(Arrays.stream(actual).toList()).isEqualTo(List.of(getResponseTaskDto()));
    }

    @WithMockUser
    @SneakyThrows
    @Test
    @DisplayName("Verify getTaskById() method works")
    @Sql(scripts = ADD_DATA_TASKS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_TASK, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getTaskById_Valid_ReturnResponseTaskDto() {
        final MvcResult result = mockMvc.perform(get(URL_TASK_1).contentType(
                MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final ResponseTaskDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ResponseTaskDto.class);
        assertThat(actual).isEqualTo(getResponseTaskDto());
    }

    @WithMockUser(roles = MANAGER)
    @SneakyThrows
    @Test
    @DisplayName("Verify updateTaskById() method works")
    @Sql(scripts = ADD_DATA_TASKS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_TASK, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateTaskById_Valid_ReturnResponseTaskDto() {
        final MvcResult result = mockMvc.perform(put(URL_TASK_1).content(objectMapper
                        .writeValueAsString(new TaskDtoCreate(UPDATE_TASK_NAME, TASK_DESCRIPTION,
                                MEDIUM, IN_PROGRESS, DUE_DATE, ONE_ID, TWO_ID))).contentType(
                                        MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        final ResponseTaskDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ResponseTaskDto.class);
        assertThat(actual).isEqualTo(getUpdatedResponseTaskDto());
    }

    @WithMockUser(roles = {MANAGER, USER})
    @SneakyThrows
    @Test
    @DisplayName("Verify deleteById() method works")
    @Sql(scripts = ADD_DATA_BEFORE_DELETE_TASK,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_TASK, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteById_Valid_Deleted() {
        mockMvc.perform(get(URL_TASK_1).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(delete(URL_TASK_1).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get(URL_TASK_1).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    private ResponseTaskDto getUpdatedResponseTaskDto() {
        final ResponseTaskDto responseTaskDto = new ResponseTaskDto();
        responseTaskDto.setId(ONE_ID);
        responseTaskDto.setName(UPDATE_TASK_NAME);
        responseTaskDto.setDescription(TASK_DESCRIPTION);
        responseTaskDto.setPriority(MEDIUM);
        responseTaskDto.setStatus(IN_PROGRESS);
        responseTaskDto.setDueDate(DUE_DATE);
        responseTaskDto.setProject(getResponseProjectDto());
        responseTaskDto.setAssignee(getUserResponseDtoWithRole());
        return responseTaskDto;
    }

    private ResponseTaskDto getResponseTaskDto() {
        final ResponseTaskDto responseTaskDto = new ResponseTaskDto();
        responseTaskDto.setId(ONE_ID);
        responseTaskDto.setName(TASK_NAME);
        responseTaskDto.setDescription(TASK_DESCRIPTION);
        responseTaskDto.setPriority(MEDIUM);
        responseTaskDto.setStatus(IN_PROGRESS);
        responseTaskDto.setDueDate(DUE_DATE);
        responseTaskDto.setProject(getResponseProjectDto());
        responseTaskDto.setAssignee(getUserResponseDtoWithRole());
        return responseTaskDto;
    }

    private UserResponseDtoWithRole getUserResponseDtoWithRole() {
        final UserResponseDtoWithRole userResponseDto = new UserResponseDtoWithRole();
        userResponseDto.setId(TWO_ID);
        userResponseDto.setUsername(USERNAME);
        userResponseDto.setEmail(EMAIL);
        userResponseDto.setFirstName(FIRST_NAME);
        userResponseDto.setLastName(LAST_NAME);
        Set<String> roleDtos = new HashSet<>();
        roleDtos.add(ROLE_USER);
        roleDtos.add(ROLE_MANAGER);
        userResponseDto.setRoleDtos(roleDtos);
        return userResponseDto;
    }

    private ResponseProjectDto getResponseProjectDto() {
        return new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, IN_PROGRESS);
    }
}
