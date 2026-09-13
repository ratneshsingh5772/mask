package com.tokenization.mask.config;

import com.tokenization.mask.security.JwtAuthenticationFilter;
import com.tokenization.mask.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        return new JwtAuthenticationFilter(jwtService, objectMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtAuthenticationFilter,
                                            ObjectMapper objectMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        // generating/decoding tokens is how a payload becomes a token in the first place
                        .requestMatchers("/api/token/**", "/api/v1/users/token/**").permitAll()
                        // Spring re-runs the filter chain for the internal error dispatch (e.g. 404/405);
                        // without this, an unauthenticated request to a bad route is masked as a 401.
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) ->
                                writeProblem(objectMapper, response, HttpStatus.UNAUTHORIZED,
                                        "A valid bearer token carrying the request payload is required",
                                        request.getRequestURI()))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeProblem(objectMapper, response, HttpStatus.FORBIDDEN,
                                        "Token role does not grant access to this resource",
                                        request.getRequestURI())))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeProblem(ObjectMapper objectMapper, jakarta.servlet.http.HttpServletResponse response,
                               HttpStatus status, String message, String path) throws java.io.IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setInstance(URI.create(path));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
