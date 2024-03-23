package mate.academy;

import mate.academy.config.SpringSecurityWebAuxTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(SpringSecurityWebAuxTestConfig.class)
class ApplicationTests {
    @Test
    void contextLoads() {
    }
}
