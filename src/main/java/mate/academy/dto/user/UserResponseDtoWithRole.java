package mate.academy.dto.user;

import java.util.Set;
import lombok.Data;

@Data
public class UserResponseDtoWithRole {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roleDtos;
}
