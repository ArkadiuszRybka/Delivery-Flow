package com.deliveryflow.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(AUTH_HEADER);
        String userId = null;
        String role = null;

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            try {
                Claims claims = jwtTokenProvider.validateAndExtractClaims(authHeader.substring(BEARER_PREFIX.length()));
                userId = claims.getSubject();
                role = claims.get("role", String.class);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(role)))
                );
            } catch (JwtException e) {
                log.warn("Rejected request with invalid JWT: {}", e.getMessage());
                writeUnauthorized(response);
                return;
            }
        }

        filterChain.doFilter(new IdentityHeaderRequestWrapper(request, userId, role), response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        problem.setTitle("Unauthorized");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
