package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.user.RoleDto;
import mate.academy.dto.user.UserRegistrationRequestDto;
import mate.academy.dto.user.UserResponseDtoWithRole;
import mate.academy.exception.RegistrationException;
import mate.academy.model.User;
import mate.academy.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users management",
        description = "Update role, receive profile info and update profile info")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Update roles",
            description = "Update roles can do only users with ROLE_ADMIN")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{userId}/role")
    public UserResponseDtoWithRole updateRole(@PathVariable Long userId,
                                              @RequestBody @Valid RoleDto roleDto) {
        return userService.updateRole(userId, roleDto);
    }

    @Operation(summary = "Receive profile info", description = "Watching information about user")
    @GetMapping("/me")
    public UserResponseDtoWithRole getProfileInfo(Authentication authentication) {
        return userService.getProfileInfo(getUserId(authentication));
    }

    @Operation(summary = "Update profile info", description = "Update information about user")
    @PatchMapping("/me")
    public UserResponseDtoWithRole updateProfileInfo(
             Authentication authentication,
            @RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.updateProfileInfo(getUserId(authentication), requestDto);
    }

    private Long getUserId(Authentication authentication) {
        final User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
