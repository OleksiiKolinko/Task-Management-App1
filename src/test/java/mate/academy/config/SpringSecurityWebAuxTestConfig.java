package mate.academy.config;

import com.dropbox.core.v2.DbxClientV2;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mate.academy.model.Role;
import mate.academy.model.User;
import mate.academy.service.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;

@TestConfiguration
@RequiredArgsConstructor
public class SpringSecurityWebAuxTestConfig {
    private static final Long TWO_ID = 2L;
    private static final Long THREE_ID = 3L;
    private static final Long FOUR_ID = 4L;
    private static final String USERNAME = "username";
    private static final String USERNAME_1 = "username1";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email@example.com";
    private static final String EMAIL_1 = "email1@example.com";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    @MockBean
    private DbxClientV2 client;
    @MockBean
    private EmailService emailService;

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        final Role role = new Role();
        role.setId(TWO_ID);
        role.setName(Role.RoleName.ROLE_MANAGER);
        final Role role1 = new Role();
        role1.setId(THREE_ID);
        role1.setName(Role.RoleName.ROLE_USER);
        final Role role2 = new Role();
        role2.setId(FOUR_ID);
        role2.setName(Role.RoleName.ROLE_ADMIN);
        final Set<Role> roles = new HashSet<>();
        roles.add(role);
        roles.add(role1);
        roles.add(role2);
        final User user = User.builder().id(TWO_ID).username(USERNAME).password(PASSWORD)
                .email(EMAIL).firstName(FIRST_NAME).lastName(LAST_NAME).roles(roles).build();
        final User user1 = User.builder().id(THREE_ID).username(USERNAME_1).password(PASSWORD)
                .email(EMAIL_1).firstName(FIRST_NAME).lastName(LAST_NAME).roles(roles).build();
        Map<String, User> users = new HashMap<>();
        users.put(USERNAME, user);
        users.put(USERNAME_1, user1);
        return users::get;
    }
}
