package com.talentradar.user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_FULL_NAME = "X-User-FullName";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Extract user information from headers
        String userId = request.getHeader(HEADER_USER_ID);
        String email = request.getHeader(HEADER_USER_EMAIL);
        String fullName = request.getHeader(HEADER_USER_FULL_NAME);
        String role = request.getHeader(HEADER_USER_ROLE);

        // If user information exists in headers, create authentication
        if (userId != null && email != null && role != null) {
            UserPrincipal userPrincipal = new UserPrincipal(userId, email, fullName, role);

            // Create authorities from role
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (role != null && !role.isEmpty()) {
                // Ensure role has ROLE_ prefix
                String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities.add(new SimpleGrantedAuthority(roleName));
            }

            // Create authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, authorities);

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
