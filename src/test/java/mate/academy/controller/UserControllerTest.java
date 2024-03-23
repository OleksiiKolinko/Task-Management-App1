package mate.academy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import lombok.SneakyThrows;
import mate.academy.config.SpringSecurityWebAuxTestConfig;
import mate.academy.dto.user.RoleDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
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
public class UserControllerTest {
    private static final Long THREE_ID = 3L;
    private static final String USERNAME = "username1";
    private static final String UPDATE_USERNAME = "updateUsername";
    private static final String EMAIL = "email1@example.com";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PASSWORD = "password";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ADMIN = "ADMIN";
    private static final String ADD_DATA_USER =
            "classpath:database/user/add-data-user-controller.sql";
    private static final String REMOVE_ALL_USER = "classpath:database/user/remove-all-user.sql";
    private static final String ADD_DATA_PROFILE_INFO =
            "classpath:database/user/add-data-profile-info-controller.sql";
    private static final String URL_USERS_3_ROLE = "/users/3/role";
    private static final String URL_USER_ME = "/users/me";
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(roles = ADMIN)
    @SneakyThrows
    @Test
    @DisplayName("Verify updateRole() method works")
    @Sql(scripts = ADD_DATA_USER, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_USER, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateRole_Valid_ReturnUserResponseDtoWithRole() {
        final MvcResult result = mockMvc.perform(put(URL_USERS_3_ROLE).content(objectMapper
                        .writeValueAsString(new RoleDto(Set.of(ROLE_MANAGER, ROLE_USER))))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final UserResponseDtoWithRole actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), UserResponseDtoWithRole.class);
        assertThat(actual).isEqualTo(getUserResponseDtoWithRole());
    }

    @WithUserDetails(USERNAME)
    @SneakyThrows
    @Test
    @DisplayName("Verify getProfileInfo() method works")
    @Sql(scripts = ADD_DATA_PROFILE_INFO, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_USER, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getProfileInfo_Valid_ReturnUserResponseDtoWithRole() {
        final MvcResult result = mockMvc.perform(get(URL_USER_ME).contentType(
                MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        final UserResponseDtoWithRole actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), UserResponseDtoWithRole.class);
        assertThat(actual).isEqualTo(getUserResponseDtoWithRole());
    }

    @WithUserDetails(USERNAME)
    @SneakyThrows
    @Test
    @DisplayName("Verify updateProfileInfo() method works")
    @Sql(scripts = ADD_DATA_PROFILE_INFO, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_USER, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateProfileInfo_Valid_ReturnUserResponseDtoWithRole() {
        final MvcResult result = mockMvc.perform(put(URL_USER_ME).content(objectMapper
                        .writeValueAsString(new UserRegistrationRequestDto(UPDATE_USERNAME,
                                PASSWORD, PASSWORD, EMAIL, FIRST_NAME, LAST_NAME))).contentType(
                                        MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        final UserResponseDtoWithRole actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), UserResponseDtoWithRole.class);
        assertThat(actual).isEqualTo(getUpdatedUserResponseDtoWithRole());
    }

    private UserResponseDtoWithRole getUpdatedUserResponseDtoWithRole() {
        final UserResponseDtoWithRole userResponseDto = new UserResponseDtoWithRole();
        userResponseDto.setId(THREE_ID);
        userResponseDto.setUsername(UPDATE_USERNAME);
        userResponseDto.setEmail(EMAIL);
        userResponseDto.setFirstName(FIRST_NAME);
        userResponseDto.setLastName(LAST_NAME);
        Set<String> roleDtos = new HashSet<>();
        roleDtos.add(ROLE_MANAGER);
        roleDtos.add(ROLE_USER);
        userResponseDto.setRoleDtos(roleDtos);
        return userResponseDto;
    }

    private UserResponseDtoWithRole getUserResponseDtoWithRole() {
        final UserResponseDtoWithRole userResponseDto = new UserResponseDtoWithRole();
        userResponseDto.setId(THREE_ID);
        userResponseDto.setUsername(USERNAME);
        userResponseDto.setEmail(EMAIL);
        userResponseDto.setFirstName(FIRST_NAME);
        userResponseDto.setLastName(LAST_NAME);
        Set<String> roleDtos = new HashSet<>();
        roleDtos.add(ROLE_MANAGER);
        roleDtos.add(ROLE_USER);
        userResponseDto.setRoleDtos(roleDtos);
        return userResponseDto;
    }
}
