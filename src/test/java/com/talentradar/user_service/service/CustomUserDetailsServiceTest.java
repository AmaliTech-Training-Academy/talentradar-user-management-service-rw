package com.talentradar.user_service.service;

import com.talentradar.user_service.model.CustomUserDetails;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private Role testRole;
    private final UUID testUserId = UUID.randomUUID();
    private final UUID testRoleId = UUID.randomUUID();
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setRoleName("USER");

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole(testRole);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should successfully load user by email")
    void loadUserByUsername_WithValidEmail_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof CustomUserDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) result;
        assertEquals(testUserId, customUserDetails.getUserId());
        assertEquals(testEmail, customUserDetails.getEmail());
        assertEquals(testEmail, customUserDetails.getUsername()); // Username is email
        assertEquals("encodedPassword", customUserDetails.getPassword());
        assertEquals("Test User", customUserDetails.getUser().getFullName());
        assertEquals("USER", customUserDetails.getRoleName());
        assertTrue(customUserDetails.isEnabled());
        assertTrue(customUserDetails.isAccountNonExpired());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());

        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should successfully load admin user")
    void loadUserByUsername_WithAdminUser_ReturnsAdminUserDetails() {
        // Arrange
        testRole.setRoleName("ADMIN");
        testUser.setRole(testRole);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof CustomUserDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) result;
        assertEquals("ADMIN", customUserDetails.getRoleName());

        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void loadUserByUsername_WithInvalidEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(testEmail));

        assertEquals("User not found with email: " + testEmail, exception.getMessage());
        verify(userRepository, atLeastOnce()).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should handle inactive user correctly")
    void loadUserByUsername_WithInactiveUser_ReturnsUserDetailsWithCorrectStatus() {
        // Arrange
        testUser.setStatus(User.UserStatus.INACTIVE);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof CustomUserDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) result;
        assertEquals(testUserId, customUserDetails.getUserId());
        assertEquals(testEmail, customUserDetails.getEmail());

        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should handle empty email")
    void loadUserByUsername_WithEmptyEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(emptyEmail));

        assertEquals("User not found with email: " + emptyEmail, exception.getMessage());
        verify(userRepository, atLeastOnce()).findByEmail(emptyEmail);
    }

    @Test
    @DisplayName("Should handle null email")
    void loadUserByUsername_WithNullEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(null));

        assertEquals("User not found with email: null", exception.getMessage());
        verify(userRepository, atLeastOnce()).findByEmail(null);
    }
}