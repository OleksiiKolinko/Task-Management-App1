package mate.academy.repository;

import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.Role;
import mate.academy.repository.role.RoleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTest {
    private static final Long ONE_ID = 1L;
    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Verify findByName() method works")
    public void findByName_ValidRole_ReturnRole() {
        final Role actual = roleRepository.findByName(Role.RoleName.WITHOUT_ROLE).orElseThrow(
                () -> new EntityNotFoundException("Can't find role by name WITHOUT_ROLE"));
        Assertions.assertEquals(ONE_ID, actual.getId());
        Assertions.assertEquals(Role.RoleName.WITHOUT_ROLE, actual.getName());
    }
}
