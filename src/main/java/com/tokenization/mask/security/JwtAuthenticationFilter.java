package com.tokenization.mask.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Validates the bearer JWT on every request. A valid token both authenticates the
 * caller (subject/role become the Spring Security principal and authority) and
 * carries the request payload, which is stashed for {@link TokenBodyArgumentResolver}.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtService.parseClaims(token);
            String principal = String.valueOf(claims.getOrDefault("userId", claims.getSubject()));
            String role = String.valueOf(claims.getOrDefault("role", "USER"));
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            var authentication = new UsernamePasswordAuthenticationToken(principal, token, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(TokenBodyArgumentResolver.RAW_TOKEN_ATTRIBUTE, token);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            writeProblem(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token: " + ex.getMessage(), request.getRequestURI());
        }
    }

    private void writeProblem(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setInstance(URI.create(path));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
