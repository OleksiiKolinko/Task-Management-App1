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

    public UserLoginResponseDto authentication(UserLoginRequestDto requestDto) {
        final String nameRequest = requestDto.username();
        final User user = userRepository.findByUsername(nameRequest).orElseThrow(
                () -> new EntityNotFoundException("Didn't find user with username "
                        + nameRequest));
        checkPassword(requestDto.password(), user.getPassword());
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.username(),
                        requestDto.password()));
        return new UserLoginResponseDto(jwtUtil.generateToken(authentication.getName()));
    }

    private void checkPassword(String requestDtoPassword, String userPassword) {
        if (!passwordEncoder.matches(requestDtoPassword, userPassword)) {
            throw new EntityNotFoundException("Wrong password");
        }
    }
}
