package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.CompleteRegistrationRequest;
import com.talentradar.user_service.dto.InviteUserRequest;
import com.talentradar.user_service.exception.InvalidTokenException;
import com.talentradar.user_service.exception.ResourceAlreadyExistsException;
import com.talentradar.user_service.exception.ResourceNotFoundException;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.model.User.UserStatus;
import com.talentradar.user_service.repository.RoleRepository;
import com.talentradar.user_service.repository.UserRepository;
import com.talentradar.user_service.service.interfaces.EmailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Value("${app.registration.token.secret}")
    private String registrationTokenSecret;
    
    @Value("${app.registration.token.expiration-ms}")
    private long registrationTokenExpirationMs;
    
    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public User initiateRegistration(InviteUserRequest request) {
        // Check if user with email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already in use");
        }

        // Find the role by ID
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.getRoleId()));

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setRole(role);
        // Status is set to INACTIVE by default in the @PrePersist method
        
        User savedUser = userRepository.save(user);
        
        // Generate and send registration token
        String token = generateRegistrationToken(savedUser);
        String inviteLink = createInvitationLink(token);
        
        // Send email with the invite link
        emailService.sendRegistrationInvite(savedUser.getEmail(), inviteLink);
        
        return savedUser;
    }
    
    @Transactional
    public User completeRegistration(String token, CompleteRegistrationRequest request) {
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidTokenException("Token cannot be empty");
        }
        
        // Validate and parse the token
        Claims claims = parseAndValidateToken(token);
        
        try {
            // Get user ID from token
            UUID userId = UUID.fromString(claims.getSubject());
            
            // Find the user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    
            // Check if user is already active
            if (user.getStatus() == UserStatus.ACTIVE) {
                throw new IllegalStateException("User is already active");
            }
            
            // Validate password and confirm password match
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Password and confirm password do not match");
            }
            
            // Update user details
            user.setFullName(request.getFullName());
            user.setUsername(generateUsername(request.getFullName()));
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setStatus(UserStatus.ACTIVE);
            
            return userRepository.save(user);
            
        } catch (IllegalArgumentException ex) {
            log.error("Error during registration completion: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid user ID in token");
        }
    }
    
    private String generateRegistrationToken(User user) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(registrationTokenExpirationMs);
        
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roleId", user.getRole().getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
    
    private Claims parseAndValidateToken(String token) {
        try {
            // For JJWT 0.11.x
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(registrationTokenSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            log.error("Token has expired: {}", ex.getMessage());
            throw new InvalidTokenException("Registration token has expired");
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            log.error("Invalid token: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid registration token");
        }
    }
    
    private javax.crypto.SecretKey getSigningKey() {
        byte[] keyBytes = registrationTokenSecret.getBytes(StandardCharsets.UTF_8);
        return new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
    }
    
    private String createInvitationLink(String token) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/auth/complete-registration")
                .queryParam("token", token)
                .toUriString();
    }
    
    private String generateUsername(String fullName) {
        // Convert to lowercase and replace spaces with dots
        String baseUsername = fullName.trim().toLowerCase().replaceAll("\\s+", ".");
        String username = baseUsername;
        int counter = 1;
        
        // Ensure username is unique
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
    
    /**
     * Validates a registration token and returns the associated user if valid.
     * @param token The registration token to validate
     * @return The user associated with the token
     * @throws InvalidTokenException if the token is invalid or expired
     */
    public User validateRegistrationToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidTokenException("Token cannot be empty");
        }
        
        Claims claims = parseAndValidateToken(token);
        
        try {
            UUID userId = UUID.fromString(claims.getSubject());
            return userRepository.findById(userId)
                    .orElseThrow(() -> new InvalidTokenException("User not found for this token"));
        } catch (IllegalArgumentException ex) {
            log.error("Invalid user ID in token: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid user ID in token");
        }
    }
}
