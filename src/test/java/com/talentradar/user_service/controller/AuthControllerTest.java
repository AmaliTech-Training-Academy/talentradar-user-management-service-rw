package com.talentradar.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.user_service.dto.CompleteRegistrationRequest;
import com.talentradar.user_service.dto.InviteUserRequest;
import com.talentradar.user_service.exception.GlobalExceptionHandler;
import com.talentradar.user_service.exception.InvalidTokenException;
import com.talentradar.user_service.exception.ResourceAlreadyExistsException;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.model.User.UserStatus;
import com.talentradar.user_service.service.interfaces.EmailService;
import com.talentradar.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private AuthController authController;

    private User testUser;
    private InviteUserRequest inviteRequest;
    private CompleteRegistrationRequest completeRequest;
    private final String TEST_TOKEN = "test.registration.token";
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final UUID TEST_ROLE_ID = UUID.randomUUID();
    private final String TEST_EMAIL = "test@example.com";
    private final String BASE_URL = "/api/v1/auth";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Setup test user
        Role role = new Role();
        role.setId(TEST_ROLE_ID);
        role.setRoleName("USER");

        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setRole(role);
        testUser.setStatus(UserStatus.INACTIVE);
        // Setup invite request
        inviteRequest = createInviteRequest(TEST_EMAIL, TEST_ROLE_ID);

        // Setup complete registration request
        completeRequest = new CompleteRegistrationRequest();
        completeRequest.setFullName("Test User");
        completeRequest.setPassword("securePassword123!");
        completeRequest.setConfirmPassword("securePassword123!");
    }

    @Test
    @DisplayName("Should successfully invite user with valid data")
    void inviteUser_WithValidData_ReturnsCreated() throws Exception {
        // Arrange
        when(userService.initiateRegistration(any(InviteUserRequest.class))).thenAnswer(invocation -> {
            // Simulate email sending after user is saved
            emailService.sendRegistrationInvite(TEST_EMAIL, "http://test-invite-link");
            return testUser;
        });

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(userService).initiateRegistration(any(InviteUserRequest.class));
        verify(emailService).sendRegistrationInvite(eq(TEST_EMAIL), anyString());
    }

    @Test
    @DisplayName("Should return bad request for invalid email")
    void inviteUser_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        inviteRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors[0].field").value("email"));

        verify(userService, never()).initiateRegistration(any(InviteUserRequest.class));
        verify(emailService, never()).sendRegistrationInvite(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return bad request for missing role ID")
    void inviteUser_WithMissingRoleId_ReturnsBadRequest() throws Exception {
        // Arrange
        inviteRequest.setRoleId(null);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors[0].field").value("roleId"));

        verify(userService, never()).initiateRegistration(any(InviteUserRequest.class));
        verify(emailService, never()).sendRegistrationInvite(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return conflict for existing email")
    void inviteUser_WithExistingEmail_ReturnsConflict() throws Exception {
        // Arrange
        when(userService.initiateRegistration(any(InviteUserRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Email already in use"));

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use"));

        verify(emailService, never()).sendRegistrationInvite(anyString(), anyString());
    }

    @Test
    @DisplayName("Should complete registration with valid token")
    void completeRegistration_WithValidToken_ReturnsOk() throws Exception {
        // Arrange
        User activeUser = new User();
        activeUser.setId(TEST_USER_ID);
        activeUser.setEmail(TEST_EMAIL);
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setFullName("Test User");
        activeUser.setUsername("test.user");
        Role role = new Role();
        role.setRoleName("USER");
        activeUser.setRole(role);

        when(userService.completeRegistration(eq(TEST_TOKEN), any(CompleteRegistrationRequest.class)))
                .thenReturn(activeUser);

        // Act & Assert
        mockMvc.perform(patch(BASE_URL + "/complete-registration")
                .param("token", TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.roleName").value("USER"));

        verify(userService).completeRegistration(eq(TEST_TOKEN), any(CompleteRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should return bad request for invalid token during completion")
    void completeRegistration_WithInvalidToken_ReturnsBadRequest() throws Exception {
        // Arrange
        when(userService.completeRegistration(eq("invalid-token"), any(CompleteRegistrationRequest.class)))
                .thenThrow(new InvalidTokenException("Invalid or expired token"));

        // Act & Assert
        mockMvc.perform(patch(BASE_URL + "/complete-registration")
                .param("token", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
    
    @Test
    @DisplayName("Should return bad request when passwords don't match")
    void completeRegistration_WithMismatchedPasswords_ReturnsBadRequest() throws Exception {
        // Arrange
        completeRequest.setConfirmPassword("differentPassword");
        
        // Act & Assert
        mockMvc.perform(patch(BASE_URL + "/complete-registration")
                .param("token", TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password and confirmation do not match"));
        
        verify(userService, never()).completeRegistration(anyString(), any(CompleteRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should validate registration token successfully")
    void validateRegistrationToken_WithValidToken_ReturnsUserEmail() throws Exception {
        // Arrange
        when(userService.validateRegistrationToken(TEST_TOKEN)).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/validate-registration-token")
                .param("token", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.status").value("VALID"));

        verify(userService).validateRegistrationToken(TEST_TOKEN);
    }

    @Test
    @DisplayName("Should return error for invalid registration token")
    void validateRegistrationToken_WithInvalidToken_ReturnsError() throws Exception {
        // Arrange
        when(userService.validateRegistrationToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid or expired token"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/validate-registration-token")
                .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verify(userService).validateRegistrationToken("invalid-token");
    }
    
    @Test
    @DisplayName("Should return bad request when token is missing")
    void validateRegistrationToken_WithMissingToken_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/validate-registration-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter 'token' for method parameter type String is not present"));
        
        verify(userService, never()).validateRegistrationToken(anyString());
    }

    private InviteUserRequest createInviteRequest(String email, UUID roleId) {
        InviteUserRequest inviteRequest = new InviteUserRequest();
        inviteRequest.setEmail(email);
        inviteRequest.setRoleId(roleId);
        return inviteRequest;
    }

    private User createTestUser(UUID id, String email, UserStatus status) {
        User testUser = new User();
        testUser.setId(id);
        testUser.setEmail(email);
        testUser.setStatus(status);
        return testUser;
    }
}
