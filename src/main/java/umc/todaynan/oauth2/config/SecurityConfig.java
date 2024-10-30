package umc.todaynan.oauth2.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import umc.todaynan.apiPayload.ApiResponse;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.converter.UserConverter;
import umc.todaynan.oauth2.JwtAuthFilter;
import umc.todaynan.oauth2.OAuth2SuccessHandler;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.oauth2.authority.CustomAuthorityMapper;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.service.UserService.CustomOAuth2UserService;
import umc.todaynan.service.UserService.CustomOidcUserService;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final UserConverter userConverter;
    @Bean
    SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/token/**", "/user/signup/**", "/user/login/**", "/health", "/suggest/**" , "/hot/**").permitAll()
                        .requestMatchers("/actuator/prometheus", "/metrics", "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())) // Use custom entry point
                .addFilterBefore(new JwtAuthFilter(tokenService, userRepository,userConverter), UsernamePasswordAuthenticationFilter.class)

                .logout(logout -> logout
                        .logoutSuccessUrl("/logout"))
        ;

        return http.build();
    }
    @Bean
    public GrantedAuthoritiesMapper customAuthorityMapper() {
        return new CustomAuthorityMapper();
    }

    // Custom Authentication Entry Point
    public static class CustomAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             org.springframework.security.core.AuthenticationException authException) throws IOException, IOException {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
        }

        @Override
        public void afterPropertiesSet() {
            setRealmName("MY_REALM"); // Customize this if needed
            super.afterPropertiesSet();
        }
    }
}
