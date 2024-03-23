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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import mate.academy.config.SpringSecurityWebAuxTestConfig;
import mate.academy.dto.label.CreateLabelDto;
import mate.academy.dto.label.ResponseLabelDto;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = SpringSecurityWebAuxTestConfig.class)
public class LabelControllerTest {
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
    private static final Long TWO_ID = 2L;
    private static final String INITIATED = "INITIATED";
    private static final String COLOR = "color";
    private static final String LABEL_NANE = "labelName";
    private static final String UPDATE_LABEL_NAME = "updateLabelName";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ID = "id";
    private static final String URL_LABELS = "/labels";
    private static final String URL_LABEL_1 = "/labels/1";
    private static final String ADD_DATA_CREATE_LABEL =
            "classpath:database/label/add-data-create-label.sql";
    private static final String REMOVE_ALL_LABEL = "classpath:database/label/remove-all-label.sql";
    private static final String ADD_DATA_FIND_ALL = "classpath:database/label/add-data-label.sql";
    private static final String MANAGER = "MANAGER";
    private static final String USER = "USER";
    private static final String SIZE = "$.*";
    private static MockMvc mockMvc;
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
    @DisplayName("Verify addComment() method works")
    @Sql(scripts = ADD_DATA_CREATE_LABEL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_LABEL, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createLabel_Valid_ReturnResponseLabelDto() {
        final MvcResult result = mockMvc.perform(post(URL_LABELS).content(objectMapper
                                .writeValueAsString(new CreateLabelDto(LABEL_NANE, COLOR,
                                        Set.of(ONE_ID)))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        final ResponseLabelDto actual = objectMapper.readValue(result.getResponse()
                        .getContentAsString(), ResponseLabelDto.class);
        assertThat(actual).usingRecursiveComparison().ignoringFields(ID)
                .isEqualTo(getResponseLabelDto());
    }

    @WithMockUser
    @SneakyThrows
    @Test
    @DisplayName("Verify findAll() method works")
    @Sql(scripts = ADD_DATA_FIND_ALL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_LABEL, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAll_Valid_ReturnListResponseLabelDto() {
        final MvcResult result = mockMvc.perform(get(URL_LABELS).contentType(
                MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final ResponseLabelDto[] actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), ResponseLabelDto[].class);
        assertThat(Arrays.stream(actual).toList()).isEqualTo(List.of(getResponseLabelDto()));
    }

    @WithMockUser(roles = MANAGER)
    @SneakyThrows
    @Test
    @DisplayName("Verify updateById() method works")
    @Sql(scripts = ADD_DATA_FIND_ALL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_LABEL, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateById_Valid_ReturnResponseLabelDto() {
        final MvcResult result = mockMvc.perform(put(URL_LABEL_1).content(objectMapper
                        .writeValueAsString(new CreateLabelDto(UPDATE_LABEL_NAME, COLOR,
                                Set.of(ONE_ID)))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        final ResponseLabelDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), ResponseLabelDto.class);
        assertThat(actual).isEqualTo(new ResponseLabelDto(
                ONE_ID, UPDATE_LABEL_NAME, COLOR, Set.of(getResponseTaskDto())));
    }

    @WithMockUser(roles = {MANAGER, USER})
    @SneakyThrows
    @Test
    @DisplayName("Verify deleteById() method works")
    @Sql(scripts = ADD_DATA_FIND_ALL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_LABEL, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteById_Valid_Deleted() {
        mockMvc.perform(get(URL_LABELS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath(SIZE, hasSize(ONE)));
        mockMvc.perform(delete(URL_LABEL_1).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get(URL_LABELS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath(SIZE, hasSize(ZERO)));
    }

    private ResponseLabelDto getResponseLabelDto() {
        return new ResponseLabelDto(ONE_ID, LABEL_NANE, COLOR, Set.of(getResponseTaskDto()));
    }

    private ResponseTaskDto getResponseTaskDto() {
        final ResponseTaskDto responseTaskDto = new ResponseTaskDto();
        responseTaskDto.setId(ONE_ID);
        responseTaskDto.setName(TASK_NAME);
        responseTaskDto.setDescription(TASK_DESCRIPTION);
        responseTaskDto.setPriority(MEDIUM);
        responseTaskDto.setStatus(IN_PROGRESS);
        responseTaskDto.setDueDate(DUE_DATE);
        responseTaskDto.setProject(new ResponseProjectDto(ONE_ID, PROJECT_NAME, PROJECT_DESCRIPTION,
                START_DATE, END_DATE, INITIATED));
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
}
