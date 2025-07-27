package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.LoginRequestDto;
import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.dto.UserDto;
import com.talentradar.user_service.model.CustomUserDetails;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UUID testUserId;
    private User testUser;
    private Role testRole;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setRoleName("USER");

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setRole(testRole);

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should successfully authenticate user and return token")
    void login_WithValidCredentials_ReturnsTokenAndUserData() {
        // Arrange
        String expectedToken = "jwt.token.here";

        when(userDetails.getUserId()).thenReturn(testUserId);
        when(userDetails.getEmail()).thenReturn("test@example.com");
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getUser()).thenReturn(testUser);
        when(userDetails.getRoleName()).thenReturn("USER");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtTokenFromUserId(userDetails)).thenReturn(expectedToken);

        // Act
        Map<String, Object> result = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result.get("token"));

        ResponseDto loginResponse = (ResponseDto) result.get("loginResponse");
        assertNotNull(loginResponse);
        assertTrue(loginResponse.getStatus());
        assertEquals("Login successful", loginResponse.getMessage());
        assertNull(loginResponse.getErrors());

        Map<String, UserDto> userMap = (Map<String, UserDto>) loginResponse.getData();
        UserDto userDto = userMap.get("user");
        assertEquals(testUserId, userDto.getId());
        assertEquals("test@example.com", userDto.getEmail());
        assertEquals("testuser", userDto.getUsername());
        assertEquals("Test User", userDto.getFullName());
        assertEquals("USER", userDto.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtTokenFromUserId(userDetails);
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when authentication fails")
    void login_WithInvalidCredentials_ThrowsBadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authenticationService.login(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateJwtTokenFromUserId(any());
    }

    @Test
    @DisplayName("Should handle authentication with different user roles")
    void login_WithAdminUser_ReturnsCorrectRole() {
        // Arrange
        String expectedToken = "jwt.token.here";
        testRole.setRoleName("ADMIN");

        when(userDetails.getUserId()).thenReturn(testUserId);
        when(userDetails.getEmail()).thenReturn("admin@example.com");
        when(userDetails.getUsername()).thenReturn("adminuser");
        when(userDetails.getUser()).thenReturn(testUser);
        when(userDetails.getRoleName()).thenReturn("ADMIN");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtTokenFromUserId(userDetails)).thenReturn(expectedToken);

        // Act
        Map<String, Object> result = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result.get("token"));

        ResponseDto loginResponse = (ResponseDto) result.get("loginResponse");
        Map<String, UserDto> userMap = (Map<String, UserDto>) loginResponse.getData();
        UserDto userDto = userMap.get("user");
        assertEquals("ADMIN", userDto.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtTokenFromUserId(userDetails);
    }

    @Test
    @DisplayName("Should verify authentication token contains correct credentials")
    void login_VerifiesAuthenticationTokenContainsCorrectCredentials() {
        // Arrange
        String expectedToken = "jwt.token.here";

        when(userDetails.getUserId()).thenReturn(testUserId);
        when(userDetails.getEmail()).thenReturn("test@example.com");
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getUser()).thenReturn(testUser);
        when(userDetails.getRoleName()).thenReturn("USER");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtTokenFromUserId(userDetails)).thenReturn(expectedToken);

        // Act
        authenticationService.login(loginRequest);

        // Assert
        verify(authenticationManager)
                .authenticate(argThat(token -> token instanceof UsernamePasswordAuthenticationToken &&
                        loginRequest.getEmail().equals(token.getPrincipal()) &&
                        loginRequest.getPassword().equals(token.getCredentials())));
    }
}