package mate.academy.security;

import lombok.RequiredArgsConstructor;
import mate.academy.dto.user.UserLoginRequestDto;
import mate.academy.dto.user.UserLoginResponseDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.User;
import mate.academy.repository.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserLoginResponseDto authentication(UserLoginRequestDto request) {
        final String nameRequest = request.username();
        final User user = userRepository.findByUsername(nameRequest).orElseThrow(
                () -> new EntityNotFoundException("Don't find user with username "
                        + nameRequest)
        );
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new EntityNotFoundException("Wrong password");
        }
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        String token = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(token);
    }
}
