package mate.academy.repository;

import java.util.Set;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.User;
import mate.academy.repository.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    private static final Long ONE_ID = 1L;
    private static final Long TWO_ID = 2L;
    private static final Long THREE_ID = 3L;
    private static final int TWO = 2;
    private static final String EMAIL = "email@example.com";
    private static final String USER_NAME = "username";
    private static final String ADD_DATA_USERS = "classpath:database/user/add-data-users.sql";
    private static final String REMOVE_ALL_USER = "classpath:database/user/remove-all-user.sql";
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Verify findByEmail() method works")
    @Sql(scripts = ADD_DATA_USERS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_USER, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByEmail_ValidEmail_ReturnUser() {
        final User actual = userRepository.findByEmail(EMAIL).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by email email@example.com"));
        Assertions.assertEquals(TWO_ID, actual.getId());
        Assertions.assertEquals(EMAIL, actual.getEmail());
        Assertions.assertEquals(USER_NAME, actual.getUsername());
    }

    @Test
    @DisplayName("Verify findByEmail() method works")
    @Sql(scripts = ADD_DATA_USERS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_USER, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByUsername_ValidUser_ReturnUser() {
        final User actual = userRepository.findByUsername(USER_NAME).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by username user"));
        Assertions.assertEquals(TWO_ID, actual.getId());
        Assertions.assertEquals(EMAIL, actual.getEmail());
        Assertions.assertEquals(USER_NAME, actual.getUsername());
    }

    @Test
    @DisplayName("Verify findByEmail() method works")
    @Sql(scripts = ADD_DATA_USERS, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = REMOVE_ALL_USER, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByRolesId_ValidAll_ReturnUsers() {
        final Set<User> actual = userRepository.findByRolesId(THREE_ID);
        Assertions.assertEquals(TWO, actual.size());
        Assertions.assertEquals(USER_NAME, actual.stream()
                .filter(u -> u.getId().equals(TWO_ID))
                .findFirst().orElseThrow(
                        () -> new EntityNotFoundException("Can't find users with role id 2L"))
                .getUsername());
        Assertions.assertEquals("admin", actual.stream()
                .filter(u -> u.getId().equals(ONE_ID))
                .findFirst().orElseThrow(
                        () -> new EntityNotFoundException("Can't find users with role id 1L"))
                .getUsername());
    }
}
