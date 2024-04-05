package mate.academy.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import mate.academy.annotation.FieldMatch;
import org.hibernate.validator.constraints.Length;

@FieldMatch(first = "password", second = "repeatPassword")
public record UserRegistrationRequestDto(@NotBlank
                                         String username,
                                         @Pattern(regexp = "^[0-9a-zA-Z]+$")
                                         @NotBlank
                                         @Length(min = 8, max = 30)
                                         String password,
                                         String repeatPassword,
                                         @NotBlank
                                         @Email
                                         String email,
                                         @NotBlank
                                         String firstName,
                                         @NotBlank
                                         String lastName) {
}
