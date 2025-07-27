package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.CompleteRegistrationRequest;
import com.talentradar.user_service.dto.InviteUserRequest;
import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.exception.InvalidTokenException;
import com.talentradar.user_service.exception.ResourceAlreadyExistsException;
import com.talentradar.user_service.exception.ResourceNotFoundException;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.repository.RoleRepository;
import com.talentradar.user_service.repository.UserRepository;
import com.talentradar.user_service.service.interfaces.EmailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private final UUID testUserId = UUID.randomUUID();
    private final UUID testRoleId = UUID.randomUUID();
    private final String testEmail = "test@example.com";
    private final String testSecret = "testSecretKeyForJWTTokenGenerationAndValidation";
    private final String testBaseUrl = "http://localhost:8080";

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
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Set up reflection for private fields
        ReflectionTestUtils.setField(userService, "registrationTokenSecret", testSecret);
        ReflectionTestUtils.setField(userService, "registrationTokenExpirationMs", 3600000L); // 1 hour
        ReflectionTestUtils.setField(userService, "baseUrl", testBaseUrl);
    }

    @Test
    @DisplayName("Should get current user successfully")
    void getMe_WithValidUserId_ReturnsUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        ResponseDto result = userService.getMe(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("User retrieved successfully", result.getMessage());
        assertNotNull(result.getData());

        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw exception when user not found in getMe")
    void getMe_WithInvalidUserId_ThrowsException() {
        // Arrange
        UUID invalidUserId = UUID.randomUUID();
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.getMe(invalidUserId));
        verify(userRepository).findById(invalidUserId);
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void getAllUsers_WithValidPagination_ReturnsUsers() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        // Act
        ResponseDto result = userService.getAllUsers(1, 10, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Users retrieved successfully", result.getMessage());
        assertNotNull(result.getData());

        verify(userRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should get users filtered by role")
    void getAllUsers_WithRoleFilter_ReturnsFilteredUsers() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(userRepository.findByRole(eq(testRole), any(PageRequest.class))).thenReturn(userPage);

        // Act
        ResponseDto result = userService.getAllUsers(1, 10, testRoleId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Users retrieved successfully", result.getMessage());

        verify(roleRepository).findById(testRoleId);
        verify(userRepository).findByRole(eq(testRole), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when role not found in getAllUsers")
    void getAllUsers_WithInvalidRoleId_ThrowsException() {
        // Arrange
        UUID invalidRoleId = UUID.randomUUID();
        when(roleRepository.findById(invalidRoleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getAllUsers(1, 10, invalidRoleId));
        verify(roleRepository).findById(invalidRoleId);
    }

    @Test
    @DisplayName("Should handle pagination with page 0")
    void getAllUsers_WithPageZero_HandlesCorrectly() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        // Act
        ResponseDto result = userService.getAllUsers(0, 10, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        verify(userRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void initiateRegistration_WithExistingEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act & Assert
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(testRoleId);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.initiateRegistration(request));
        verify(userRepository).findByEmail(testEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when role not found in initiateRegistration")
    void initiateRegistration_WithInvalidRoleId_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        // Act & Assert
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(testRoleId);

        assertThrows(ResourceNotFoundException.class, () -> userService.initiateRegistration(request));
        verify(userRepository).findByEmail(testEmail);
        verify(roleRepository).findById(testRoleId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully initiate registration for regular user")
    void initiateRegistration_WithValidRequest_SuccessfullyCreatesUser() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());

        // Act
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(testRoleId);

        User result = userService.initiateRegistration(request);

        // Assert
        assertNotNull(result);
        verify(userRepository).findByEmail(testEmail);
        verify(roleRepository).findById(testRoleId);
        verify(userRepository).save(any(User.class));
        verify(emailService).sendRegistrationInvite(anyString(), anyString());
    }

    @Test
    @DisplayName("Should successfully initiate registration for developer and send event")
    void initiateRegistration_WithDeveloperRole_SendsEvent() {
        // Arrange
        Role developerRole = new Role();
        developerRole.setId(UUID.randomUUID());
        developerRole.setRoleName("DEVELOPER");

        Role managerRole = new Role();
        managerRole.setId(UUID.randomUUID());
        managerRole.setRoleName("MANAGER");

        User managerUser = new User();
        managerUser.setId(UUID.randomUUID());
        managerUser.setRole(managerRole);

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(roleRepository.findById(any())).thenReturn(Optional.of(developerRole));
        when(roleRepository.findByRoleName("MANAGER")).thenReturn(Optional.of(managerRole));
        when(userRepository.findByRole(managerRole)).thenReturn(List.of(managerUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(developerRole.getId());

        User result = userService.initiateRegistration(request);

        // Assert
        assertNotNull(result);
        verify(rabbitTemplate).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should successfully initiate registration for manager and send event")
    void initiateRegistration_WithManagerRole_SendsEvent() {
        // Arrange
        Role managerRole = new Role();
        managerRole.setId(UUID.randomUUID());
        managerRole.setRoleName("MANAGER");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(roleRepository.findById(any())).thenReturn(Optional.of(managerRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(managerRole.getId());

        User result = userService.initiateRegistration(request);

        // Assert
        assertNotNull(result);
        verify(rabbitTemplate).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should throw exception when no manager found for developer")
    void initiateRegistration_WithDeveloperRole_NoManagerFound_ThrowsException() {
        // Arrange
        Role developerRole = new Role();
        developerRole.setId(UUID.randomUUID());
        developerRole.setRoleName("DEVELOPER");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(testEmail);
        savedUser.setRole(developerRole);

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(roleRepository.findById(any())).thenReturn(Optional.of(developerRole));
        when(roleRepository.findByRoleName("MANAGER")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());

        // Act & Assert
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(developerRole.getId());

        assertThrows(ResourceNotFoundException.class, () -> userService.initiateRegistration(request));
    }

    @Test
    @DisplayName("Should throw exception when no manager users found for developer")
    void initiateRegistration_WithDeveloperRole_NoManagerUsers_ThrowsException() {
        // Arrange
        Role developerRole = new Role();
        developerRole.setId(UUID.randomUUID());
        developerRole.setRoleName("DEVELOPER");

        Role managerRole = new Role();
        managerRole.setId(UUID.randomUUID());
        managerRole.setRoleName("MANAGER");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(testEmail);
        savedUser.setRole(developerRole);

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(roleRepository.findById(any())).thenReturn(Optional.of(developerRole));
        when(roleRepository.findByRoleName("MANAGER")).thenReturn(Optional.of(managerRole));
        when(userRepository.findByRole(managerRole)).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());

        // Act & Assert
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(developerRole.getId());

        assertThrows(ResourceNotFoundException.class, () -> userService.initiateRegistration(request));
    }

    @Test
    @DisplayName("Should complete registration successfully")
    void completeRegistration_WithValidToken_SuccessfullyCompletesRegistration() {
        // Arrange
        String token = generateValidToken();
        CompleteRegistrationRequest request = new CompleteRegistrationRequest();
        request.setFullName("John Doe");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        User inactiveUser = new User();
        inactiveUser.setId(testUserId);
        inactiveUser.setEmail(testEmail);
        inactiveUser.setStatus(User.UserStatus.INACTIVE);
        inactiveUser.setRole(testRole);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(inactiveUser);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        User result = userService.completeRegistration(token, request);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(rabbitTemplate).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should throw exception when token is null")
    void completeRegistration_WithNullToken_ThrowsException() {
        // Arrange
        CompleteRegistrationRequest request = new CompleteRegistrationRequest();
        request.setFullName("John Doe");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.completeRegistration(null, request));
    }

    @Test
    @DisplayName("Should throw exception when token is empty")
    void completeRegistration_WithEmptyToken_ThrowsException() {
        // Arrange
        CompleteRegistrationRequest request = new CompleteRegistrationRequest();
        request.setFullName("John Doe");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.completeRegistration("", request));
    }

    @Test
    @DisplayName("Should throw exception when user not found during completion")
    void completeRegistration_WithInvalidToken_UserNotFound_ThrowsException() {
        // Arrange
        String token = generateValidToken();
        CompleteRegistrationRequest request = new CompleteRegistrationRequest();
        request.setFullName("John Doe");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.completeRegistration(token, request));
    }

    @Test
    @DisplayName("Should throw exception when user is already active")
    void completeRegistration_WithActiveUser_ThrowsException() {
        // Arrange
        String token = generateValidToken();
        CompleteRegistrationRequest request = new CompleteRegistrationRequest();
        request.setFullName("John Doe");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        User activeUser = new User();
        activeUser.setId(testUserId);
        activeUser.setEmail(testEmail);
        activeUser.setStatus(User.UserStatus.ACTIVE);
        activeUser.setRole(testRole);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(activeUser));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> userService.completeRegistration(token, request));
    }

    @Test
    @DisplayName("Should throw exception when passwords do not match")
    void completeRegistration_WithMismatchedPasswords_ThrowsException() {
        // Arrange
        String token = generateValidToken();
        CompleteRegistrationRequest request = new CompleteRegistrationRequest();
        request.setFullName("John Doe");
        request.setPassword("password123");
        request.setConfirmPassword("differentPassword");

        User inactiveUser = new User();
        inactiveUser.setId(testUserId);
        inactiveUser.setEmail(testEmail);
        inactiveUser.setStatus(User.UserStatus.INACTIVE);
        inactiveUser.setRole(testRole);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.completeRegistration(token, request));
    }

    @Test
    @DisplayName("Should handle username generation with conflicts")
    void completeRegistration_WithUsernameConflict_GeneratesUniqueUsername() {
        // Arrange
        String token = generateValidToken();
        CompleteRegistrationRequest request = new CompleteRegistrationRequest();
        request.setFullName("John Doe");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        User inactiveUser = new User();
        inactiveUser.setId(testUserId);
        inactiveUser.setEmail(testEmail);
        inactiveUser.setStatus(User.UserStatus.INACTIVE);
        inactiveUser.setRole(testRole);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(new User()));
        when(userRepository.findByUsername("john.doe1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(inactiveUser);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        User result = userService.completeRegistration(token, request);

        // Assert
        assertNotNull(result);
        verify(userRepository).findByUsername("john.doe");
        verify(userRepository).findByUsername("john.doe1");
    }

    @Test
    @DisplayName("Should validate registration token successfully")
    void validateRegistrationToken_WithValidToken_ReturnsUser() {
        // Arrange
        String token = generateValidToken();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.validateRegistrationToken(token);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw exception when token is null in validation")
    void validateRegistrationToken_WithNullToken_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.validateRegistrationToken(null));
    }

    @Test
    @DisplayName("Should throw exception when token is empty in validation")
    void validateRegistrationToken_WithEmptyToken_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.validateRegistrationToken(""));
    }

    @Test
    @DisplayName("Should throw exception when user not found during token validation")
    void validateRegistrationToken_WithInvalidToken_UserNotFound_ThrowsException() {
        // Arrange
        String token = generateValidToken();
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.validateRegistrationToken(token));
    }

    @Test
    @DisplayName("Should throw exception when token is expired")
    void validateRegistrationToken_WithExpiredToken_ThrowsException() {
        // Arrange
        String expiredToken = generateExpiredToken();

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.validateRegistrationToken(expiredToken));
    }

    @Test
    @DisplayName("Should throw exception when token is invalid")
    void validateRegistrationToken_WithInvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.validateRegistrationToken(invalidToken));
    }

    // Helper methods for token generation
    private String generateValidToken() {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(3600000L); // 1 hour

        return Jwts.builder()
                .subject(testUserId.toString())
                .claim("email", testEmail)
                .claim("roleId", testRoleId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private String generateExpiredToken() {
        Instant now = Instant.now();
        Instant expiryDate = now.minusMillis(3600000L); // 1 hour ago

        return Jwts.builder()
                .subject(testUserId.toString())
                .claim("email", testEmail)
                .claim("roleId", testRoleId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private javax.crypto.SecretKey getSigningKey() {
        byte[] keyBytes = testSecret.getBytes(StandardCharsets.UTF_8);
        return new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
    }
}