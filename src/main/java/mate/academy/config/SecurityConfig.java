package mate.academy.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.RequiredArgsConstructor;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.security.CustomUserDetailsService;
import mate.academy.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private static final String CLIENT_IDENTIFIER = "Task_management_app1/0.1";
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${dropbox.client.id.app.key}")
    private String clientIdAppKey;
    @Value("${dropbox.client.app.secret}")
    private String clientAppSecret;

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/auth/**", "/swagger-ui/**")
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                ).httpBasic(withDefaults())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userDetailsService)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DbxClientV2 dropboxClient() {
        final DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFIER);
        final DbxWebAuth webAuth =
                new DbxWebAuth(config, new DbxAppInfo(clientIdAppKey, clientAppSecret));
        final DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder()
                .withNoRedirect()
                .withTokenAccessType(TokenAccessType.OFFLINE)
                .build();
        System.out.println(new StringBuilder("1. Go to ").append(webAuth.authorize(webAuthRequest))
                .append(System.lineSeparator())
                .append("2. Click \"Allow\" (you might have to log in first).")
                .append(System.lineSeparator())
                .append("3. Copy the authorization code."));
        System.out.print("Enter the authorization code here: ");
        final DbxAuthFinish authFinish;
        try {
            authFinish = webAuth.finishFromCode(new BufferedReader(new InputStreamReader(System.in))
                    .readLine().trim());
        } catch (DbxException | IOException e) {
            throw new EntityNotFoundException("Wrong dropbox authorization code");
        }
        return new DbxClientV2(config, new DbxCredential(authFinish.getAccessToken(),
                authFinish.getExpiresAt(), authFinish.getRefreshToken(), clientIdAppKey,
                clientAppSecret));
    }
}
