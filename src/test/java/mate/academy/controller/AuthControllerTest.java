package mate.academy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import lombok.SneakyThrows;
import mate.academy.config.SpringSecurityWebAuxTestConfig;
import mate.academy.dto.user.UserLoginRequestDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDtoWithRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = SpringSecurityWebAuxTestConfig.class)
public class AuthControllerTest {
    private static final String EMAIL = "emailAuth@example.com";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String WITHOUT_ROLE = "WITHOUT_ROLE";
    private static final String USER = "userAuth";
    private static final String ID = "id";
    private static final Long TWO_ID = 2L;
    private static final String PASSWORD = "password";
    private static final String REMOVE_USER_AFTER_REGISTER =
            "classpath:database/user/remove-all-user.sql";
    private static final String URL_REGISTER = "/auth/register";
    private static final String URL_LOGIN = "/auth/login";
    private static final String ADMIN = "admin";
    private static final String PASSWORD_ADMIN = "12345678";
    private static final int LENGTH = 143;
    private static MockMvc mockMvc;
    @MockBean
    private AuthenticationManager authenticationManager;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify register() method works")
    @Sql(scripts = REMOVE_USER_AFTER_REGISTER,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void register_Valid_ReturnUserResponseDtoWithRole() {
        final UserResponseDtoWithRole expect = getUserResponseDtoWithRole();
        final UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(USER, PASSWORD,
                PASSWORD, EMAIL, FIRST_NAME, LAST_NAME);
        final String jsonRequest = objectMapper.writeValueAsString(requestDto);
        final MvcResult result = mockMvc.perform(post(URL_REGISTER).content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andReturn();
        final UserResponseDtoWithRole actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), UserResponseDtoWithRole.class);
        assertThat(actual).isNotNull();
        assertThat(actual).usingRecursiveComparison().ignoringFields(ID).isEqualTo(expect);
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify login() method works")
    public void login_Valid_ReturnUserLoginResponseDto() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(ADMIN, PASSWORD_ADMIN));
        final MvcResult result = mockMvc.perform(post(URL_LOGIN).content(objectMapper
                                .writeValueAsString(new UserLoginRequestDto(ADMIN, PASSWORD_ADMIN)))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString().length()).isEqualTo(LENGTH);
    }

    private UserResponseDtoWithRole getUserResponseDtoWithRole() {
        final UserResponseDtoWithRole userResponseDto = new UserResponseDtoWithRole();
        userResponseDto.setId(TWO_ID);
        userResponseDto.setUsername(USER);
        userResponseDto.setEmail(EMAIL);
        userResponseDto.setFirstName(FIRST_NAME);
        userResponseDto.setLastName(LAST_NAME);
        Set<String> roleDtos = new HashSet<>();
        roleDtos.add(WITHOUT_ROLE);
        userResponseDto.setRoleDtos(roleDtos);
        return userResponseDto;
    }
}
